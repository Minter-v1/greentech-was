package com.greentech.payroll.controller;

import com.greentech.account.domain.AppRole;
import com.greentech.common.dto.res.ApiResult;
import com.greentech.common.dto.res.PageResult;
import com.greentech.payroll.dto.req.PayrollCalculateReq;
import com.greentech.payroll.dto.res.PayrollRunRes;
import com.greentech.payroll.dto.res.PayslipRes;
import com.greentech.payroll.dto.res.PayslipSummaryRes;
import com.greentech.payroll.service.PayrollService;
import com.greentech.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "08 급여", description = "월별 급여 정산 실행 및 명세서 조회")
@RestController
@RequestMapping("/api/v1/payrolls")
@RequiredArgsConstructor
public class PayrollController {

    private static final String HR_OR_ADMIN =
            "hasAnyAuthority('" + AppRole.ADMIN + "', '" + AppRole.HR + "')";

    private final PayrollService payrollService;

    @Operation(summary = "급여 정산 실행",
            description = "재직자 전체를 대상으로 지급·공제를 계산해 명세서 생성. 같은 정산월 재실행 시 기존 결과 대체")
    @PreAuthorize(HR_OR_ADMIN)
    @PostMapping("/runs")
    public ApiResult<PayrollRunRes> calculate(@Valid @RequestBody PayrollCalculateReq request) {
        PayrollRunRes result = payrollService.calculate(request, SecurityUtils.currentUsername());
        return ApiResult.ok(result, "급여 정산이 완료되었습니다");
    }

    @Operation(summary = "급여 정산 목록 조회")
    @PreAuthorize(HR_OR_ADMIN)
    @GetMapping("/runs")
    public ApiResult<PageResult<PayrollRunRes>> findRuns(@PageableDefault(size = 12) Pageable pageable) {
        return ApiResult.ok(payrollService.findRuns(pageable));
    }

    @Operation(summary = "급여 정산 단건 조회")
    @PreAuthorize(HR_OR_ADMIN)
    @GetMapping("/runs/{runId}")
    public ApiResult<PayrollRunRes> findRun(@PathVariable Long runId) {
        return ApiResult.ok(payrollService.findRun(runId));
    }

    @Operation(summary = "급여 정산 확정", description = "확정 이후에는 재계산 불가")
    @PreAuthorize(HR_OR_ADMIN)
    @PostMapping("/runs/{runId}/confirm")
    public ApiResult<PayrollRunRes> confirm(@PathVariable Long runId) {
        PayrollRunRes result = payrollService.confirm(runId, SecurityUtils.currentUsername());
        return ApiResult.ok(result, "급여 정산이 확정되었습니다");
    }

    @Operation(summary = "정산 대상 명세서 목록 조회")
    @PreAuthorize(HR_OR_ADMIN)
    @GetMapping("/runs/{runId}/payslips")
    public ApiResult<List<PayslipSummaryRes>> findPayslips(@PathVariable Long runId) {
        return ApiResult.ok(payrollService.findPayslips(runId));
    }

    @Operation(summary = "내 급여 명세서 목록 조회")
    @GetMapping("/payslips/me")
    public ApiResult<List<PayslipSummaryRes>> findMyPayslips() {
        return ApiResult.ok(payrollService.findMyPayslips(SecurityUtils.currentEmployeeId()));
    }

    @Operation(summary = "급여 명세서 상세 조회", description = "지급·공제 항목별 금액 포함")
    @GetMapping("/payslips/{payslipId}")
    public ApiResult<PayslipRes> findPayslip(@PathVariable Long payslipId) {
        return ApiResult.ok(payrollService.findPayslip(payslipId));
    }
}
