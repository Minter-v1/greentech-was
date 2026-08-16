package com.greentech.payroll.dto.res;

import com.greentech.payroll.domain.Payslip;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "급여 명세서")
public record PayslipRes(
        @Schema(description = "명세서 ID", example = "1")
        Long id,

        @Schema(description = "정산월", example = "2026-08")
        String payYearMonth,

        @Schema(description = "사원 ID", example = "7")
        Long employeeId,

        @Schema(description = "사번", example = "20220501")
        String empNo,

        @Schema(description = "성명", example = "오세훈")
        String employeeName,

        @Schema(description = "부서명 - 정산 시점 스냅샷", example = "생산1팀")
        String departmentName,

        @Schema(description = "직위명 - 정산 시점 스냅샷", example = "사원")
        String positionName,

        @Schema(description = "근무일수", example = "20")
        int workDays,

        @Schema(description = "연장 근무 분", example = "150")
        int overtimeMinutes,

        @Schema(description = "지급 합계", example = "3700000")
        BigDecimal grossPay,

        @Schema(description = "공제 합계", example = "470000")
        BigDecimal totalDeduction,

        @Schema(description = "실지급액", example = "3230000")
        BigDecimal netPay,

        @Schema(description = "항목별 상세")
        List<PayslipItemRes> items) {

    public static PayslipRes from(Payslip entity) {
        return new PayslipRes(
                entity.getId(),
                entity.getPayrollRun().getPayYearMonth(),
                entity.getEmployee().getId(),
                entity.getEmpNo(),
                entity.getEmployeeName(),
                entity.getDepartmentName(),
                entity.getPositionName(),
                entity.getWorkDays(),
                entity.getOvertimeMinutes(),
                entity.getGrossPay(),
                entity.getTotalDeduction(),
                entity.getNetPay(),
                entity.getDetails().stream().map(PayslipItemRes::from).toList());
    }
}
