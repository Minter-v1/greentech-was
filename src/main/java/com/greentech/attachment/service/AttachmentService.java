package com.greentech.attachment.service;

import com.greentech.account.domain.AppRole;
import com.greentech.attachment.domain.Attachment;
import com.greentech.attachment.dto.res.AttachmentRes;
import com.greentech.attachment.repository.AttachmentRepository;
import com.greentech.common.exception.BusinessException;
import com.greentech.common.exception.ErrorCode;
import com.greentech.security.SecurityUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 첨부파일 저장 및 조회
 *
 * NOTE: 바이너리는 파일시스템, 메타데이터만 DB 보관
 * NOTE: 저장 경로는 yyyy/MM/dd 로 분산해 단일 디렉터리 파일 수 증가 방지
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentService {

    private static final DateTimeFormatter DATE_PATH = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final AttachmentRepository attachmentRepository;
    private final StorageProperties storageProperties;

    @Transactional(readOnly = true)
    public List<AttachmentRes> findByOwner(Attachment.OwnerType ownerType, Long ownerId) {
        ensureReadable(ownerType, ownerId);
        return attachmentRepository.findByOwnerTypeAndOwnerIdOrderByIdDesc(ownerType, ownerId).stream()
                .map(AttachmentRes::from)
                .toList();
    }

    /**
     * 첨부파일 열람 권한 검증
     *
     * NOTE: 식별자만 알면 타인의 증명서·스캔본이 노출되므로 소유자 확인 필요
     * TODO: CERTIFICATE 등 사원 외 소유 타입은 현재 인사담당자만 열람 가능
     *       본인 자격증 스캔본 열람이 필요해지면 owner 를 사원까지 역추적하는 규칙 추가
     */
    private void ensureReadable(Attachment.OwnerType ownerType, Long ownerId) {
        if (SecurityUtils.hasRole(AppRole.ADMIN) || SecurityUtils.hasRole(AppRole.HR)) {
            return;
        }
        Long currentEmployeeId = SecurityUtils.currentEmployeeIdOrNull();
        boolean ownedByCurrentUser = ownerType == Attachment.OwnerType.EMPLOYEE
                && currentEmployeeId != null
                && currentEmployeeId.equals(ownerId);

        if (!ownedByCurrentUser) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인의 첨부파일만 조회할 수 있습니다");
        }
    }

    @Transactional
    public AttachmentRes upload(
            Attachment.OwnerType ownerType,
            Long ownerId,
            Attachment.Category category,
            MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.EMPTY_FILE);
        }

        String relativePath = buildRelativePath(file.getOriginalFilename());
        Path target = resolve(relativePath);

        try {
            Files.createDirectories(target.getParent());
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("첨부파일 저장 실패 path={}", target, e);
            throw new BusinessException(ErrorCode.FILE_STORAGE_ERROR);
        }

        Attachment attachment = Attachment.builder()
                .ownerType(ownerType)
                .ownerId(ownerId)
                .category(category)
                .originalName(safeName(file.getOriginalFilename()))
                .storedPath(relativePath)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .checksum(checksum(target))
                .build();

        return AttachmentRes.from(attachmentRepository.save(attachment));
    }

    @Transactional(readOnly = true)
    public DownloadFile download(Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> BusinessException.notFound(ErrorCode.ATTACHMENT_NOT_FOUND, attachmentId));
        ensureReadable(attachment.getOwnerType(), attachment.getOwnerId());

        Path path = resolve(attachment.getStoredPath());
        if (!Files.exists(path)) {
            log.error("첨부파일 실물 누락 id={} path={}", attachmentId, path);
            throw new BusinessException(ErrorCode.ATTACHMENT_NOT_FOUND, "저장된 파일을 찾을 수 없습니다");
        }

        return new DownloadFile(
                new FileSystemResource(path), attachment.getOriginalName(), attachment.getContentType());
    }

    @Transactional
    public void delete(Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> BusinessException.notFound(ErrorCode.ATTACHMENT_NOT_FOUND, attachmentId));

        // NOTE: 메타데이터 삭제 우선 - 실물 삭제 실패가 트랜잭션을 되돌리지 않도록 분리
        attachmentRepository.delete(attachment);

        try {
            Files.deleteIfExists(resolve(attachment.getStoredPath()));
        } catch (IOException e) {
            log.warn("첨부파일 실물 삭제 실패 id={} path={}", attachmentId, attachment.getStoredPath(), e);
        }
    }

    // MARK: 내부 헬퍼

    private Path resolve(String relativePath) {
        Path root = Path.of(storageProperties.root()).toAbsolutePath().normalize();
        Path resolved = root.resolve(relativePath).normalize();
        // NOTE: 경로 조작으로 루트 밖을 참조하는 요청 차단
        if (!resolved.startsWith(root)) {
            throw new BusinessException(ErrorCode.FILE_STORAGE_ERROR, "허용되지 않은 저장 경로입니다");
        }
        return resolved;
    }

    private String buildRelativePath(String originalName) {
        String extension = extensionOf(originalName);
        return "%s/%s%s".formatted(LocalDate.now().format(DATE_PATH), UUID.randomUUID(), extension);
    }

    private String extensionOf(String originalName) {
        if (originalName == null) {
            return "";
        }
        int dot = originalName.lastIndexOf('.');
        if (dot < 0 || dot == originalName.length() - 1) {
            return "";
        }
        return originalName.substring(dot).toLowerCase();
    }

    private String safeName(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return "unnamed";
        }
        return Path.of(originalName).getFileName().toString();
    }

    private String checksum(Path path) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(Files.readAllBytes(path)));
        } catch (Exception e) {
            log.warn("체크섬 계산 실패 path={}", path, e);
            return null;
        }
    }

    /** 다운로드 응답 구성 요소 */
    public record DownloadFile(Resource resource, String originalName, String contentType) {
    }
}
