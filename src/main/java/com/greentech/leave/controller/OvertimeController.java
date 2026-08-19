package com.greentech.leave.controller;

import com.greentech.account.domain.AppRole;
import com.greentech.common.dto.res.ApiResult;
import com.greentech.common.dto.res.PageResult;
import com.greentech.common.enums.ApprovalStatus;
import com.greentech.leave.dto.req.OvertimeCreateReq;
import com.greentech.leave.dto.res.OvertimeRequestRes;
import com.greentech.leave.service.OvertimeService;
import com.greentech.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

@Tag(name = "07 연장근무", description = "연장·야간·휴일 근무 신청 및 결재")
@RestController
@RequestMapping("/api/v1/overtimes")
@RequiredArgsConstructor
public class OvertimeController {

    private static final String APPROVER =
            "hasAnyAuthority('" + AppRole.ADMIN + "', '" + AppRole.HR + "', '" + AppRole.MANAGER + "')";

    private final OvertimeService overtimeService;

    @Operation(summary = "연장근무 신청", description = "승인된 건만 급여 정산에서 수당으로 반영")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResult<OvertimeRequestRes> create(@Valid @RequestBody OvertimeCreateReq request) {
        OvertimeRequestRes created = overtimeService.create(SecurityUtils.currentEmployeeId(), request);
        return ApiResult.ok(created, "연장근무가 신청되었습니다");
    }

    @Operation(summary = "내 연장근무 신청 목록 조회")
    @GetMapping("/me")
    public ApiResult<PageResult<OvertimeRequestRes>> findMyRequests(
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResult.ok(overtimeService.findMyRequests(SecurityUtils.currentEmployeeId(), pageable));
    }

    @Operation(summary = "결재 상태별 신청 목록 조회")
    @PreAuthorize(APPROVER)
    @GetMapping
    public ApiResult<PageResult<OvertimeRequestRes>> findByStatus(
            @Parameter(description = "결재 상태") @RequestParam(defaultValue = "REQUESTED") ApprovalStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResult.ok(overtimeService.findByStatus(
                status, SecurityUtils.currentEmployeeIdOrNull(), pageable));
    }

    @Operation(summary = "연장근무 승인")
    @PreAuthorize(APPROVER)
    @PostMapping("/{id}/approve")
    public ApiResult<OvertimeRequestRes> approve(@PathVariable Long id) {
        return ApiResult.ok(
                overtimeService.approve(id, SecurityUtils.currentEmployeeIdOrNull()), "연장근무가 승인되었습니다");
    }

    @Operation(summary = "연장근무 반려")
    @PreAuthorize(APPROVER)
    @PostMapping("/{id}/reject")
    public ApiResult<OvertimeRequestRes> reject(@PathVariable Long id) {
        return ApiResult.ok(
                overtimeService.reject(id, SecurityUtils.currentEmployeeIdOrNull()), "연장근무가 반려되었습니다");
    }
}
