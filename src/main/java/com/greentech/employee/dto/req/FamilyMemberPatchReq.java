package com.greentech.employee.dto.req;

import com.greentech.employee.domain.FamilyMember.Relation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.openapitools.jackson.nullable.JsonNullable;

@Schema(description = "가족사항 부분 수정 요청")
public record FamilyMemberPatchReq(
        @Schema(description = "성명", example = "홍아들")
        JsonNullable<@NotBlank(message = "성명은 비울 수 없습니다") @Size(max = 50) String> name,

        @Schema(description = "관계", example = "CHILD")
        JsonNullable<@NotNull(message = "관계는 비울 수 없습니다") Relation> relation,

        @Schema(description = "생년월일", example = "2018-05-05")
        JsonNullable<LocalDate> birthDate,

        @Schema(description = "부양가족 여부", example = "true")
        JsonNullable<@NotNull(message = "부양가족 여부는 비울 수 없습니다") Boolean> dependent,

        @Schema(description = "동거 여부", example = "true")
        JsonNullable<@NotNull(message = "동거 여부는 비울 수 없습니다") Boolean> cohabiting) {
}
