package com.greentech.employee.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.openapitools.jackson.nullable.JsonNullable;

@Schema(description = "자격증 부분 수정 요청")
public record CertificatePatchReq(
        @Schema(description = "자격증명", example = "산업안전기사")
        JsonNullable<@NotBlank(message = "자격증명은 비울 수 없습니다") @Size(max = 100) String> name,

        @Schema(description = "발급기관", example = "한국산업인력공단")
        JsonNullable<@Size(max = 100) String> issuer,

        @Schema(description = "자격증 번호", example = "20-1-123456")
        JsonNullable<@Size(max = 100) String> licenseNo,

        @Schema(description = "취득일", example = "2020-11-20")
        JsonNullable<LocalDate> acquiredDate,

        @Schema(description = "만료일", example = "2030-11-19")
        JsonNullable<LocalDate> expiryDate) {
}
