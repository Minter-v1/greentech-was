package com.greentech.employee.dto.res;

import com.greentech.employee.domain.Education;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "학력")
public record EducationRes(
        @Schema(description = "학력 ID", example = "1")
        Long id,

        @Schema(description = "학교명", example = "한국대학교")
        String schoolName,

        @Schema(description = "전공", example = "기계공학")
        String major,

        @Schema(description = "학위", example = "BACHELOR")
        Education.Degree degree,

        @Schema(description = "입학일")
        LocalDate admissionDate,

        @Schema(description = "졸업일")
        LocalDate graduationDate,

        @Schema(description = "졸업 여부", example = "true")
        boolean graduated) {

    public static EducationRes from(Education entity) {
        return new EducationRes(
                entity.getId(),
                entity.getSchoolName(),
                entity.getMajor(),
                entity.getDegree(),
                entity.getAdmissionDate(),
                entity.getGraduationDate(),
                entity.isGraduated());
    }
}
