package com.greentech.common.dto.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

/** 전체 API 공통 응답 형식. 성공과 실패 모두 동일 구조 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "공통 응답 형식")
public record ApiResult<T>(
        @Schema(description = "성공 여부", example = "true")
        boolean success,

        @Schema(description = "결과 코드", example = "OK")
        String code,

        @Schema(description = "결과 메시지", example = "요청이 처리되었습니다")
        String message,

        @Schema(description = "응답 본문")
        T data,

        @Schema(description = "필드 단위 검증 오류 - 실패 시에만 포함")
        List<FieldErrorRes> fieldErrors,

        @Schema(description = "요청 경로 - 실패 시에만 포함", example = "/api/v1/employees/999")
        String path,

        @Schema(description = "응답 생성 시각")
        LocalDateTime timestamp) {

    private static final String CODE_OK = "OK";
    private static final String MESSAGE_OK = "요청이 처리되었습니다";

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(true, CODE_OK, MESSAGE_OK, data, null, null, LocalDateTime.now());
    }

    public static <T> ApiResult<T> ok(T data, String message) {
        return new ApiResult<>(true, CODE_OK, message, data, null, null, LocalDateTime.now());
    }

    public static ApiResult<Void> ok() {
        return new ApiResult<>(true, CODE_OK, MESSAGE_OK, null, null, null, LocalDateTime.now());
    }

    public static ApiResult<Void> error(String code, String message, String path) {
        return new ApiResult<>(false, code, message, null, null, path, LocalDateTime.now());
    }

    public static ApiResult<Void> error(
            String code, String message, String path, List<FieldErrorRes> fieldErrors) {
        return new ApiResult<>(false, code, message, null, fieldErrors, path, LocalDateTime.now());
    }
}
