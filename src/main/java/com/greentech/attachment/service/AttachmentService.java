package com.greentech.attachment.service;

import com.greentech.account.domain.AppRole;
import com.greentech.attachment.domain.Attachment;
import com.greentech.attachment.dto.res.AttachmentRes;
import com.greentech.attachment.repository.AttachmentRepository;
import com.greentech.attachment.storage.FileStorage;
import com.greentech.common.exception.BusinessException;
import com.greentech.common.exception.ErrorCode;
import com.greentech.security.SecurityUtils;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/** 첨부파일 저장 및 조회 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentService {

    private static final DateTimeFormatter DATE_PATH = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final AttachmentRepository attachmentRepository;
    private final FileStorage fileStorage;

    @Transactional(readOnly = true)
    public List<AttachmentRes> findByOwner(Attachment.OwnerType ownerType, Long ownerId) {
        ensureReadable(ownerType, ownerId);
        return attachmentRepository.findByOwnerTypeAndOwnerIdOrderByIdDesc(ownerType, ownerId).stream()
                .map(AttachmentRes::from)
                .toList();
    }

    // TODO: 사원 외 소유 타입은 인사담당자만 열람 가능
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

        String objectKey = buildObjectKey(file.getOriginalFilename());
        FileStorage.StoredFile storedFile = fileStorage.store(objectKey, file);

        Attachment attachment = Attachment.builder()
                .ownerType(ownerType)
                .ownerId(ownerId)
                .category(category)
                .originalName(safeName(file.getOriginalFilename()))
                .storedPath(storedFile.objectKey())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .checksum(storedFile.checksum())
                .build();

        try {
            return AttachmentRes.from(attachmentRepository.saveAndFlush(attachment));
        } catch (RuntimeException e) {
            deleteStoredFile(storedFile.objectKey(), attachment.getId());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public DownloadFile download(Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> BusinessException.notFound(ErrorCode.ATTACHMENT_NOT_FOUND, attachmentId));
        ensureReadable(attachment.getOwnerType(), attachment.getOwnerId());

        return new DownloadFile(
                fileStorage.load(attachment.getStoredPath()),
                attachment.getOriginalName(),
                attachment.getContentType(),
                attachment.getFileSize());
    }

    @Transactional
    public void delete(Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> BusinessException.notFound(ErrorCode.ATTACHMENT_NOT_FOUND, attachmentId));

        attachmentRepository.delete(attachment);
        deleteStoredFile(attachment.getStoredPath(), attachmentId);
    }

    private void deleteStoredFile(String objectKey, Long attachmentId) {
        try {
            fileStorage.delete(objectKey);
        } catch (RuntimeException e) {
            log.warn("첨부파일 실물 삭제 실패 id={} key={}", attachmentId, objectKey, e);
        }
    }

    // MARK: 내부 헬퍼

    private String buildObjectKey(String originalName) {
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

    /** 다운로드 응답 구성 요소 */
    public record DownloadFile(Resource resource, String originalName, String contentType, long fileSize) {
    }
}
