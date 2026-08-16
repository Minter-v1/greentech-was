package com.greentech.employee.dto.res;

import com.greentech.employee.domain.Certificate;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "자격증")
public record CertificateRes(
        @Schema(description = "자격증 ID", example = "1")
        Long id,

        @Schema(description = "자격증명", example = "산업안전기사")
        String name,

        @Schema(description = "발급기관", example = "한국산업인력공단")
        String issuer,

        @Schema(description = "자격증 번호")
        String licenseNo,

        @Schema(description = "취득일")
        LocalDate acquiredDate,

        @Schema(description = "만료일")
        LocalDate expiryDate) {

    public static CertificateRes from(Certificate entity) {
        return new CertificateRes(
                entity.getId(),
                entity.getName(),
                entity.getIssuer(),
                entity.getLicenseNo(),
                entity.getAcquiredDate(),
                entity.getExpiryDate());
    }
}
