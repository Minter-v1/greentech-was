package com.greentech.employee.dto.req;

import com.greentech.employee.domain.FamilyMember;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "가족사항 등록 요청")
public record FamilyMemberCreateReq(
        @Schema(description = "성명", example = "홍아들")
        @NotBlank(message = "성명은 필수입니다")
        @Size(max = 50)
        String name,

        @Schema(description = "관계", example = "CHILD")
        @NotNull(message = "관계는 필수입니다")
        FamilyMember.Relation relation,

        @Schema(description = "생년월일", example = "2018-05-05")
        LocalDate birthDate,

        @Schema(description = "부양가족 여부", example = "true")
        Boolean dependent,

        @Schema(description = "동거 여부", example = "true")
        Boolean cohabiting) {
}
