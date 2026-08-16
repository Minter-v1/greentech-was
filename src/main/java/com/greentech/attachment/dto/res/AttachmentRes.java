package com.greentech.attachment.dto.res;

import com.greentech.attachment.domain.Attachment;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "첨부파일 메타데이터")
public record AttachmentRes(
        @Schema(description = "첨부파일 ID", example = "1")
        Long id,

        @Schema(description = "소유 도메인", example = "EMPLOYEE")
        Attachment.OwnerType ownerType,

        @Schema(description = "소유 대상 ID", example = "7")
        Long ownerId,

        @Schema(description = "분류", example = "PROOF_DOCUMENT")
        Attachment.Category category,

        @Schema(description = "원본 파일명", example = "재직증명서.pdf")
        String originalName,

        @Schema(description = "콘텐츠 타입", example = "application/pdf")
        String contentType,

        @Schema(description = "파일 크기 바이트", example = "184320")
        long fileSize,

        @Schema(description = "업로더", example = "hr01")
        String createdBy,

        @Schema(description = "업로드 시각")
        LocalDateTime createdAt) {

    public static AttachmentRes from(Attachment entity) {
        return new AttachmentRes(
                entity.getId(),
                entity.getOwnerType(),
                entity.getOwnerId(),
                entity.getCategory(),
                entity.getOriginalName(),
                entity.getContentType(),
                entity.getFileSize(),
                entity.getCreatedBy(),
                entity.getCreatedAt());
    }
}
