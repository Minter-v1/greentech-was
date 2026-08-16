package com.greentech.employee.dto.res;

import com.greentech.employee.domain.EmploymentHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "발령 이력")
public record EmploymentHistoryRes(
        @Schema(description = "이력 ID", example = "1")
        Long id,

        @Schema(description = "발령 구분", example = "TRANSFER")
        EmploymentHistory.ChangeType changeType,

        @Schema(description = "발령일", example = "2026-08-01")
        LocalDate effectiveDate,

        @Schema(description = "변경 전 부서 ID")
        Long beforeDepartmentId,

        @Schema(description = "변경 후 부서 ID")
        Long afterDepartmentId,

        @Schema(description = "변경 전 직위 ID")
        Long beforePositionId,

        @Schema(description = "변경 후 직위 ID")
        Long afterPositionId,

        @Schema(description = "사유")
        String reason) {

    public static EmploymentHistoryRes from(EmploymentHistory entity) {
        return new EmploymentHistoryRes(
                entity.getId(),
                entity.getChangeType(),
                entity.getEffectiveDate(),
                entity.getBeforeDepartmentId(),
                entity.getAfterDepartmentId(),
                entity.getBeforePositionId(),
                entity.getAfterPositionId(),
                entity.getReason());
    }
}
