package com.greentech.common.exception;

import com.greentech.common.dto.res.ApiResult;
import com.greentech.common.dto.res.FieldErrorRes;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** 예외를 공통 응답 형식으로 일원화 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResult<Void>> handleBusiness(
            BusinessException e, HttpServletRequest request) {
        ErrorCode code = e.getErrorCode();
        // NOTE: 4xx 는 정상 업무 흐름이라 스택트레이스 미기록
        log.warn("업무 예외 [{}] {} - {}", code.name(), request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(code.getStatus())
                .body(ApiResult.error(code.name(), e.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> handleValidation(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        List<FieldErrorRes> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(FieldErrorRes::from)
                .toList();
        return ResponseEntity.badRequest()
                .body(ApiResult.error(
                        ErrorCode.INVALID_REQUEST.name(),
                        ErrorCode.INVALID_REQUEST.getDefaultMessage(),
                        request.getRequestURI(),
                        fieldErrors));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResult<Void>> handleHandlerValidation(
            HandlerMethodValidationException e, HttpServletRequest request) {
        return ResponseEntity.badRequest()
                .body(ApiResult.error(
                        ErrorCode.INVALID_REQUEST.name(),
                        ErrorCode.INVALID_REQUEST.getDefaultMessage(),
                        request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResult<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String message = "파라미터 '%s' 의 값이 올바르지 않습니다".formatted(e.getName());
        return ResponseEntity.badRequest()
                .body(ApiResult.error(ErrorCode.INVALID_REQUEST.name(), message, request.getRequestURI()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResult<Void>> handleIntegrity(
            DataIntegrityViolationException e, HttpServletRequest request) {
        log.warn("무결성 제약 위반 {} - {}", request.getRequestURI(), e.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResult.error(
                        ErrorCode.CONFLICT.name(),
                        "데이터 제약 조건에 위배됩니다 - 중복 값이나 참조 관계를 확인하세요",
                        request.getRequestURI()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResult<Void>> handleAccessDenied(
            AccessDeniedException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResult.error(
                        ErrorCode.FORBIDDEN.name(),
                        ErrorCode.FORBIDDEN.getDefaultMessage(),
                        request.getRequestURI()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResult<Void>> handleAuthentication(
            AuthenticationException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResult.error(
                        ErrorCode.UNAUTHORIZED.name(),
                        ErrorCode.UNAUTHORIZED.getDefaultMessage(),
                        request.getRequestURI()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResult<Void>> handleNoResource(
            NoResourceFoundException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResult.error(
                        ErrorCode.NOT_FOUND.name(),
                        ErrorCode.NOT_FOUND.getDefaultMessage(),
                        request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleUnexpected(Exception e, HttpServletRequest request) {
        log.error("처리되지 않은 예외 {}", request.getRequestURI(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResult.error(
                        ErrorCode.INTERNAL_ERROR.name(),
                        ErrorCode.INTERNAL_ERROR.getDefaultMessage(),
                        request.getRequestURI()));
    }
}
