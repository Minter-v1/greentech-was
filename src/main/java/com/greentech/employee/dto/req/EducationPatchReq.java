package com.greentech.employee.dto.req;

import com.greentech.employee.domain.Education.Degree;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.openapitools.jackson.nullable.JsonNullable;

@Schema(description = "학력 부분 수정 요청")
public record EducationPatchReq(
        @Schema(description = "학교명", example = "한국대학교")
        JsonNullable<@NotBlank(message = "학교명은 비울 수 없습니다") @Size(max = 100) String> schoolName,

        @Schema(description = "전공", example = "기계공학")
        JsonNullable<@Size(max = 100) String> major,

        @Schema(description = "학위", example = "BACHELOR")
        JsonNullable<@NotNull(message = "학위는 비울 수 없습니다") Degree> degree,

        @Schema(description = "입학일", example = "2010-03-02")
        JsonNullable<LocalDate> admissionDate,

        @Schema(description = "졸업일", example = "2014-02-20")
        JsonNullable<LocalDate> graduationDate,

        @Schema(description = "졸업 여부", example = "true")
        JsonNullable<@NotNull(message = "졸업 여부는 비울 수 없습니다") Boolean> graduated) {
}
