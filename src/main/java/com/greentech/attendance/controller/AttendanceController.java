package com.greentech.attendance.controller;

import com.greentech.account.domain.AppRole;
import com.greentech.attendance.dto.req.HolidayCreateReq;
import com.greentech.attendance.dto.res.AttendanceMonthlyRes;
import com.greentech.attendance.dto.res.AttendanceRes;
import com.greentech.attendance.dto.res.WorkCalendarRes;
import com.greentech.attendance.service.AttendanceService;
import com.greentech.common.dto.res.ApiResult;
import com.greentech.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "05 근태", description = "출퇴근 기록 및 근무 달력")
@RestController
@RequestMapping("/api/v1/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private static final String HR_OR_ADMIN =
            "hasAnyAuthority('" + AppRole.ADMIN + "', '" + AppRole.HR + "')";

    private final AttendanceService attendanceService;

    @Operation(summary = "출근 등록", description = "토큰에 연결된 본인 사원 기준으로 당일 출근 기록 생성")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/check-in")
    public ApiResult<AttendanceRes> checkIn() {
        return ApiResult.ok(attendanceService.checkIn(SecurityUtils.currentEmployeeId()), "출근 처리되었습니다");
    }

    @Operation(summary = "퇴근 등록", description = "근무 분과 연장·야간 분을 계산해 반영")
    @PostMapping("/check-out")
    public ApiResult<AttendanceRes> checkOut() {
        return ApiResult.ok(attendanceService.checkOut(SecurityUtils.currentEmployeeId()), "퇴근 처리되었습니다");
    }

    @Operation(summary = "내 월별 근태 조회")
    @GetMapping("/me")
    public ApiResult<AttendanceMonthlyRes> findMyMonthly(
            @Parameter(description = "정산월", example = "2026-08")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {
        return ApiResult.ok(attendanceService.findMonthly(SecurityUtils.currentEmployeeId(), yearMonth));
    }

    @Operation(summary = "사원 월별 근태 조회")
    @PreAuthorize(HR_OR_ADMIN)
    @GetMapping("/employees/{employeeId}")
    public ApiResult<AttendanceMonthlyRes> findEmployeeMonthly(
            @PathVariable Long employeeId,
            @Parameter(description = "정산월", example = "2026-08")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {
        return ApiResult.ok(attendanceService.findMonthly(employeeId, yearMonth));
    }

    // MARK: 근무 달력

    @Operation(summary = "근무 달력 조회", description = "등록된 공휴일 등 특이 날짜 목록")
    @GetMapping("/calendar")
    public ApiResult<List<WorkCalendarRes>> findCalendar(
            @Parameter(description = "시작일", example = "2026-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "종료일", example = "2026-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResult.ok(attendanceService.findCalendar(from, to));
    }

    @Operation(summary = "근무 달력 등록", description = "음력 공휴일 등 매년 달라지는 날짜를 인사팀이 등록")
    @PreAuthorize(HR_OR_ADMIN)
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/calendar")
    public ApiResult<WorkCalendarRes> registerCalendarDay(@Valid @RequestBody HolidayCreateReq request) {
        return ApiResult.ok(attendanceService.registerCalendarDay(request), "근무 달력에 등록되었습니다");
    }
}
