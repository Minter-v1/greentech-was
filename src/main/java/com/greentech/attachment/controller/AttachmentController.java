package com.greentech.attachment.controller;

import com.greentech.account.domain.AppRole;
import com.greentech.attachment.domain.Attachment;
import com.greentech.attachment.dto.res.AttachmentRes;
import com.greentech.attachment.service.AttachmentService;
import com.greentech.common.dto.res.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "09 첨부파일", description = "증명서·자격증 스캔본·사진 업로드 및 다운로드")
@RestController
@RequestMapping("/api/v1/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private static final String HR_OR_ADMIN =
            "hasAnyAuthority('" + AppRole.ADMIN + "', '" + AppRole.HR + "')";

    private final AttachmentService attachmentService;

    @Operation(summary = "소유 대상별 첨부파일 목록 조회")
    @GetMapping
    public ApiResult<List<AttachmentRes>> findByOwner(
            @Parameter(description = "소유 도메인") @RequestParam Attachment.OwnerType ownerType,
            @Parameter(description = "소유 대상 ID") @RequestParam Long ownerId) {
        return ApiResult.ok(attachmentService.findByOwner(ownerType, ownerId));
    }

    @Operation(summary = "첨부파일 업로드", description = "바이너리는 스토리지에 저장하고 DB 에는 메타데이터만 기록")
    @PreAuthorize(HR_OR_ADMIN)
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResult<AttachmentRes> upload(
            @Parameter(description = "소유 도메인") @RequestParam Attachment.OwnerType ownerType,
            @Parameter(description = "소유 대상 ID") @RequestParam Long ownerId,
            @Parameter(description = "분류") @RequestParam Attachment.Category category,
            @RequestPart("file") MultipartFile file) {
        AttachmentRes created = attachmentService.upload(ownerType, ownerId, category, file);
        return ApiResult.ok(created, "첨부파일이 업로드되었습니다");
    }

    @Operation(summary = "첨부파일 다운로드")
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        AttachmentService.DownloadFile file = attachmentService.download(id);

        String encodedName = URLEncoder.encode(file.originalName(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        String contentType = file.contentType() != null
                ? file.contentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedName)
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(file.resource());
    }

    @Operation(summary = "첨부파일 삭제")
    @PreAuthorize(HR_OR_ADMIN)
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        attachmentService.delete(id);
        return ApiResult.ok(null, "첨부파일이 삭제되었습니다");
    }
}
