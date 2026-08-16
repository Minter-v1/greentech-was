package com.greentech.common.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.FieldError;

/** 필드 단위 검증 오류 */
@Schema(description = "필드 검증 오류")
public record FieldErrorRes(
        @Schema(description = "필드명", example = "empNo")
        String field,

        @Schema(description = "거부된 값", example = "")
        Object rejectedValue,

        @Schema(description = "오류 메시지", example = "사번은 필수입니다")
        String message) {

    public static FieldErrorRes from(FieldError fieldError) {
        return new FieldErrorRes(
                fieldError.getField(),
                fieldError.getRejectedValue(),
                fieldError.getDefaultMessage());
    }
}
