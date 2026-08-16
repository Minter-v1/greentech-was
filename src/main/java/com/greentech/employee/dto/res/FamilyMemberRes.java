package com.greentech.employee.dto.res;

import com.greentech.employee.domain.FamilyMember;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "가족사항")
public record FamilyMemberRes(
        @Schema(description = "가족사항 ID", example = "1")
        Long id,

        @Schema(description = "성명", example = "홍아들")
        String name,

        @Schema(description = "관계", example = "CHILD")
        FamilyMember.Relation relation,

        @Schema(description = "생년월일")
        LocalDate birthDate,

        @Schema(description = "부양가족 여부", example = "true")
        boolean dependent,

        @Schema(description = "동거 여부", example = "true")
        boolean cohabiting) {

    public static FamilyMemberRes from(FamilyMember entity) {
        return new FamilyMemberRes(
                entity.getId(),
                entity.getName(),
                entity.getRelation(),
                entity.getBirthDate(),
                entity.isDependent(),
                entity.isCohabiting());
    }
}
