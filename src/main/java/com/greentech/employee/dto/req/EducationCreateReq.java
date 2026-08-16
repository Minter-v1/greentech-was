package com.greentech.employee.dto.req;

import com.greentech.employee.domain.Education;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "학력 등록 요청")
public record EducationCreateReq(
        @Schema(description = "학교명", example = "한국대학교")
        @NotBlank(message = "학교명은 필수입니다")
        @Size(max = 100)
        String schoolName,

        @Schema(description = "전공", example = "기계공학")
        @Size(max = 100)
        String major,

        @Schema(description = "학위", example = "BACHELOR")
        @NotNull(message = "학위는 필수입니다")
        Education.Degree degree,

        @Schema(description = "입학일", example = "2010-03-02")
        LocalDate admissionDate,

        @Schema(description = "졸업일", example = "2014-02-20")
        LocalDate graduationDate,

        @Schema(description = "졸업 여부", example = "true")
        Boolean graduated) {
}
