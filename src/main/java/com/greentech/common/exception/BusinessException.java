package com.greentech.common.exception;

import lombok.Getter;

/** 업무 규칙 위반. GlobalExceptionHandler 에서 ErrorCode 의 HTTP 상태로 변환 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public static BusinessException notFound(ErrorCode code, Object id) {
        return new BusinessException(code, code.getDefaultMessage() + " (id=" + id + ")");
    }
}
