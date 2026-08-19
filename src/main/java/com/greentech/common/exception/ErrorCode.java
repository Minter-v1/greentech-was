package com.greentech.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** 업무 예외 코드 */
@Getter
public enum ErrorCode {

    // MARK: 공통
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 자원을 찾을 수 없습니다"),
    CONFLICT(HttpStatus.CONFLICT, "이미 존재하거나 현재 상태와 충돌합니다"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 처리 중 오류가 발생했습니다"),

    // MARK: 인증, 인가
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다"),
    BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다"),
    ACCOUNT_DISABLED(HttpStatus.FORBIDDEN, "사용할 수 없는 계정입니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다"),
    NO_LINKED_EMPLOYEE(HttpStatus.FORBIDDEN, "사원 정보가 연결되지 않은 계정입니다"),

    // MARK: 사원
    EMPLOYEE_NOT_FOUND(HttpStatus.NOT_FOUND, "사원을 찾을 수 없습니다"),
    DUPLICATE_EMP_NO(HttpStatus.CONFLICT, "이미 사용 중인 사번입니다"),

    // MARK: 조직
    DEPARTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "부서를 찾을 수 없습니다"),
    POSITION_NOT_FOUND(HttpStatus.NOT_FOUND, "직위를 찾을 수 없습니다"),
    DUPLICATE_CODE(HttpStatus.CONFLICT, "이미 사용 중인 코드입니다"),

    // MARK: 근태
    ALREADY_CHECKED_IN(HttpStatus.CONFLICT, "이미 출근 처리되었습니다"),
    NOT_CHECKED_IN(HttpStatus.CONFLICT, "출근 기록이 없어 퇴근 처리를 할 수 없습니다"),
    ALREADY_CHECKED_OUT(HttpStatus.CONFLICT, "이미 퇴근 처리되었습니다"),

    // MARK: 휴가
    LEAVE_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "휴가 종류를 찾을 수 없습니다"),
    LEAVE_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "휴가 신청을 찾을 수 없습니다"),
    LEAVE_BALANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 연도의 휴가 부여 내역이 없습니다"),
    INSUFFICIENT_LEAVE(HttpStatus.CONFLICT, "잔여 휴가가 부족합니다"),
    LEAVE_ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 결재가 완료된 신청입니다"),
    OVERLAPPING_LEAVE(HttpStatus.CONFLICT, "같은 기간에 이미 신청된 휴가가 있습니다"),
    INVALID_LEAVE_PERIOD(HttpStatus.BAD_REQUEST, "휴가 시작일이 종료일보다 늦을 수 없습니다"),

    // MARK: 연장근무
    OVERTIME_NOT_FOUND(HttpStatus.NOT_FOUND, "연장근무 신청을 찾을 수 없습니다"),
    OVERTIME_ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 결재가 완료된 신청입니다"),
    INVALID_OVERTIME_PERIOD(HttpStatus.BAD_REQUEST, "연장근무 종료 시각이 시작 시각보다 빠를 수 없습니다"),

    // MARK: 급여
    PAYROLL_RUN_NOT_FOUND(HttpStatus.NOT_FOUND, "급여 정산 내역을 찾을 수 없습니다"),
    PAYROLL_ALREADY_CONFIRMED(HttpStatus.CONFLICT, "이미 확정된 급여 정산은 다시 계산할 수 없습니다"),
    PAYSLIP_NOT_FOUND(HttpStatus.NOT_FOUND, "급여 명세서를 찾을 수 없습니다"),
    DEDUCTION_RATE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 연도의 공제 요율이 등록되지 않았습니다"),
    INVALID_YEAR_MONTH(HttpStatus.BAD_REQUEST, "정산월 형식이 올바르지 않습니다. (YYYY-MM)"),

    // MARK: 첨부파일
    ATTACHMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "첨부파일을 찾을 수 없습니다"),
    FILE_STORAGE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장 중 오류가 발생했습니다"),
    EMPTY_FILE(HttpStatus.BAD_REQUEST, "빈 파일은 업로드할 수 없습니다");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }
}
