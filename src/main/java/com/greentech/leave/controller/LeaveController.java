package com.greentech.leave.controller;

import com.greentech.account.domain.AppRole;
import com.greentech.common.dto.res.ApiResult;
import com.greentech.common.dto.res.PageResult;
import com.greentech.common.enums.ApprovalStatus;
import com.greentech.leave.dto.req.LeaveRejectReq;
import com.greentech.leave.dto.req.LeaveRequestCreateReq;
import com.greentech.leave.dto.res.LeaveBalanceRes;
import com.greentech.leave.dto.res.LeaveRequestRes;
import com.greentech.leave.dto.res.LeaveTypeRes;
import com.greentech.leave.service.LeaveService;
import com.greentech.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

@Tag(name = "06 휴가", description = "휴가 신청, 결재, 잔여 조회")
@RestController
@RequestMapping("/api/v1/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private static final String APPROVER =
            "hasAnyAuthority('" + AppRole.ADMIN + "', '" + AppRole.HR + "', '" + AppRole.MANAGER + "')";
    private static final String TEAM_BALANCE_READER =
            "hasAnyAuthority('" + AppRole.ADMIN + "', '" + AppRole.HR + "')"
                    + " or (hasAuthority('" + AppRole.MANAGER + "')"
                    + " and @employeeAuthorization.isManagerOf(#employeeId))";

    private final LeaveService leaveService;

    @Operation(summary = "휴가 종류 목록 조회")
    @GetMapping("/types")
    public ApiResult<List<LeaveTypeRes>> findTypes() {
        return ApiResult.ok(leaveService.findTypes());
    }

    @Operation(summary = "내 휴가 잔여 조회")
    @GetMapping("/balances/me")
    public ApiResult<List<LeaveBalanceRes>> findMyBalances(
            @Parameter(description = "기준 연도", example = "2026")
            @RequestParam(required = false) Integer year) {
        int targetYear = year != null ? year : LocalDate.now().getYear();
        return ApiResult.ok(leaveService.findBalances(SecurityUtils.currentEmployeeId(), targetYear));
    }

    @Operation(summary = "사원 휴가 잔여 조회")
    @PreAuthorize(TEAM_BALANCE_READER)
    @GetMapping("/balances/employees/{employeeId}")
    public ApiResult<List<LeaveBalanceRes>> findBalances(
            @PathVariable Long employeeId,
            @Parameter(description = "기준 연도", example = "2026")
            @RequestParam(required = false) Integer year) {
        int targetYear = year != null ? year : LocalDate.now().getYear();
        return ApiResult.ok(leaveService.findBalances(employeeId, targetYear));
    }

    @Operation(summary = "휴가 신청", description = "기간 중복 시 거부되며 신청일수는 근무일 기준으로 자동 계산")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/requests")
    public ApiResult<LeaveRequestRes> create(@Valid @RequestBody LeaveRequestCreateReq request) {
        LeaveRequestRes created = leaveService.create(SecurityUtils.currentEmployeeId(), request);
        return ApiResult.ok(created, "휴가가 신청되었습니다");
    }

    @Operation(summary = "내 휴가 신청 목록 조회")
    @GetMapping("/requests/me")
    public ApiResult<PageResult<LeaveRequestRes>> findMyRequests(
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResult.ok(leaveService.findMyRequests(SecurityUtils.currentEmployeeId(), pageable));
    }

    @Operation(summary = "휴가 신청 목록 조회", description = "결재 상태로 필터링 가능")
    @PreAuthorize(APPROVER)
    @GetMapping("/requests")
    public ApiResult<PageResult<LeaveRequestRes>> findRequests(
            @Parameter(description = "결재 상태") @RequestParam(required = false) ApprovalStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResult.ok(leaveService.findRequests(
                status, SecurityUtils.currentEmployeeIdOrNull(), pageable));
    }

    @Operation(summary = "휴가 승인", description = "연차 차감 대상이면 승인 시점에 잔여 차감")
    @PreAuthorize(APPROVER)
    @PostMapping("/requests/{id}/approve")
    public ApiResult<LeaveRequestRes> approve(@PathVariable Long id) {
        LeaveRequestRes result = leaveService.approve(id, SecurityUtils.currentEmployeeIdOrNull());
        return ApiResult.ok(result, "휴가가 승인되었습니다");
    }

    @Operation(summary = "휴가 반려")
    @PreAuthorize(APPROVER)
    @PostMapping("/requests/{id}/reject")
    public ApiResult<LeaveRequestRes> reject(
            @PathVariable Long id, @Valid @RequestBody LeaveRejectReq request) {
        LeaveRequestRes result = leaveService.reject(id, SecurityUtils.currentEmployeeIdOrNull(), request);
        return ApiResult.ok(result, "휴가가 반려되었습니다");
    }

    @Operation(summary = "휴가 취소", description = "승인 상태에서 취소하면 차감된 잔여를 복구")
    @PostMapping("/requests/{id}/cancel")
    public ApiResult<LeaveRequestRes> cancel(@PathVariable Long id) {
        LeaveRequestRes result = leaveService.cancel(id, SecurityUtils.currentEmployeeId());
        return ApiResult.ok(result, "휴가가 취소되었습니다");
    }
}
