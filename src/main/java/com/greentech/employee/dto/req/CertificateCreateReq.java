package com.greentech.employee.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "자격증 등록 요청")
public record CertificateCreateReq(
        @Schema(description = "자격증명", example = "산업안전기사")
        @NotBlank(message = "자격증명은 필수입니다")
        @Size(max = 100)
        String name,

        @Schema(description = "발급기관", example = "한국산업인력공단")
        @Size(max = 100)
        String issuer,

        @Schema(description = "자격증 번호", example = "20-1-123456")
        @Size(max = 100)
        String licenseNo,

        @Schema(description = "취득일", example = "2020-11-20")
        LocalDate acquiredDate,

        @Schema(description = "만료일", example = "2030-11-19")
        LocalDate expiryDate) {
}
