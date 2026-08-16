package com.greentech.org.dto.res;

import com.greentech.org.domain.JobPosition;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "직위 정보")
public record JobPositionRes(
        @Schema(description = "직위 ID", example = "4")
        Long id,

        @Schema(description = "직위코드", example = "P40")
        String code,

        @Schema(description = "직위명", example = "과장")
        String name,

        @Schema(description = "직위 서열", example = "4")
        int levelNo,

        @Schema(description = "사용 여부", example = "true")
        boolean active) {

    public static JobPositionRes from(JobPosition entity) {
        return new JobPositionRes(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getLevelNo(),
                entity.isActive());
    }
}
