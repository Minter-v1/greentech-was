package com.greentech.payroll.service;

import com.greentech.account.domain.AppRole;
import com.greentech.account.domain.AuditLog;
import com.greentech.account.repository.AuditLogRepository;
import com.greentech.attendance.domain.Attendance;
import com.greentech.attendance.repository.AttendanceRepository;
import com.greentech.common.dto.res.PageResult;
import com.greentech.common.enums.ApprovalStatus;
import com.greentech.common.exception.BusinessException;
import com.greentech.common.exception.ErrorCode;
import com.greentech.employee.domain.Employee;
import com.greentech.employee.repository.EmployeeRepository;
import com.greentech.leave.domain.OvertimeRequest;
import com.greentech.leave.repository.OvertimeRequestRepository;
import com.greentech.payroll.domain.DeductionRate;
import com.greentech.payroll.domain.PayItem;
import com.greentech.payroll.domain.PayrollRun;
import com.greentech.payroll.domain.Payslip;
import com.greentech.payroll.domain.PayslipDetail;
import com.greentech.payroll.domain.SalaryContract;
import com.greentech.payroll.dto.req.PayrollCalculateReq;
import com.greentech.payroll.dto.res.PayrollRunRes;
import com.greentech.payroll.dto.res.PayslipRes;
import com.greentech.payroll.dto.res.PayslipSummaryRes;
import com.greentech.payroll.repository.DeductionRateRepository;
import com.greentech.payroll.repository.PayItemRepository;
import com.greentech.payroll.repository.PayrollRunRepository;
import com.greentech.payroll.repository.PayslipRepository;
import com.greentech.payroll.repository.SalaryContractRepository;
import com.greentech.security.SecurityUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 월별 급여 정산
 *
 * NOTE: 월말 정산 대상 약 80명 규모 - 단건 트랜잭션으로 처리
 * TODO: 대상 인원이 수천 명대로 늘어나면 Spring Batch 로 청크 처리 전환 필요
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollService {

    private static final List<Attendance.Status> WORKED_STATUSES = List.of(
            Attendance.Status.NORMAL, Attendance.Status.LATE, Attendance.Status.EARLY_LEAVE);

    private final PayrollRunRepository payrollRunRepository;
    private final PayslipRepository payslipRepository;
    private final PayItemRepository payItemRepository;
    private final DeductionRateRepository deductionRateRepository;
    private final SalaryContractRepository salaryContractRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final OvertimeRequestRepository overtimeRequestRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public PageResult<PayrollRunRes> findRuns(Pageable pageable) {
        Page<PayrollRun> page = payrollRunRepository.findAllByOrderByPayYearMonthDesc(pageable);
        return PageResult.of(page, PayrollRunRes::from);
    }

    @Transactional(readOnly = true)
    public PayrollRunRes findRun(Long runId) {
        return PayrollRunRes.from(getRunOrThrow(runId));
    }

    @Transactional(readOnly = true)
    public List<PayslipSummaryRes> findPayslips(Long runId) {
        getRunOrThrow(runId);
        return payslipRepository.findByPayrollRunIdOrderByEmpNoAsc(runId).stream()
                .map(PayslipSummaryRes::from)
                .toList();
    }

    /**
     * 급여 명세서 상세 조회
     *
     * NOTE: 식별자만 알면 타인 명세서가 노출되므로 소유자 검증 필수
     */
    @Transactional(readOnly = true)
    public PayslipRes findPayslip(Long payslipId) {
        Payslip payslip = payslipRepository.findWithDetailsById(payslipId)
                .orElseThrow(() -> BusinessException.notFound(ErrorCode.PAYSLIP_NOT_FOUND, payslipId));
        ensureReadable(payslip);
        return PayslipRes.from(payslip);
    }

    private void ensureReadable(Payslip payslip) {
        if (SecurityUtils.hasRole(AppRole.ADMIN) || SecurityUtils.hasRole(AppRole.HR)) {
            return;
        }
        Long currentEmployeeId = SecurityUtils.currentEmployeeIdOrNull();
        if (currentEmployeeId == null || !currentEmployeeId.equals(payslip.getEmployee().getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인의 급여 명세서만 조회할 수 있습니다");
        }
    }

    @Transactional(readOnly = true)
    public List<PayslipSummaryRes> findMyPayslips(Long employeeId) {
        return payslipRepository.findByEmployeeIdOrderByIdDesc(employeeId).stream()
                .map(PayslipSummaryRes::from)
                .toList();
    }

    /**
     * 급여 정산 실행
     *
     * NOTE: 같은 정산월 재실행 시 기존 명세서를 삭제하고 다시 계산
     * NOTE: 확정(CONFIRMED) 상태는 재계산 차단
     */
    @Transactional
    public PayrollRunRes calculate(PayrollCalculateReq request, String executor) {
        YearMonth yearMonth = parseYearMonth(request.payYearMonth());
        LocalDate from = yearMonth.atDay(1);
        LocalDate to = yearMonth.atEndOfMonth();

        PayrollRun run = payrollRunRepository.findByPayYearMonth(request.payYearMonth())
                .orElseGet(() -> payrollRunRepository.save(PayrollRun.builder()
                        .payYearMonth(request.payYearMonth())
                        .status(PayrollRun.Status.DRAFT)
                        .build()));

        if (run.isLocked()) {
            throw new BusinessException(ErrorCode.PAYROLL_ALREADY_CONFIRMED);
        }

        // NOTE: 엔티티 단위 삭제로 payslip_detail 까지 cascade 제거
        payslipRepository.deleteAll(payslipRepository.findByPayrollRunIdOrderByEmpNoAsc(run.getId()));
        payslipRepository.flush();

        Map<String, PayItem> payItems = payItemRepository.findByActiveTrueOrderBySortOrderAsc().stream()
                .collect(Collectors.toMap(PayItem::getCode, Function.identity(), (a, b) -> a));

        Map<String, DeductionRate> rates = deductionRateRepository.findByYear(yearMonth.getYear()).stream()
                .collect(Collectors.toMap(DeductionRate::getItemCode, Function.identity(), (a, b) -> a));
        if (rates.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.DEDUCTION_RATE_NOT_FOUND,
                    "%d년 공제 요율이 등록되지 않았습니다".formatted(yearMonth.getYear()));
        }

        List<Employee> targets = employeeRepository.findByStatusOrderByEmpNoAsc(Employee.Status.ACTIVE);

        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalDeduction = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;

        for (Employee employee : targets) {
            Payslip payslip = buildPayslip(run, employee, from, to, payItems, rates);
            payslipRepository.save(payslip);

            totalGross = totalGross.add(payslip.getGrossPay());
            totalDeduction = totalDeduction.add(payslip.getTotalDeduction());
            totalNet = totalNet.add(payslip.getNetPay());
        }

        run.setStatus(PayrollRun.Status.CALCULATED);
        run.setPayDate(request.payDate());
        run.setTargetCount(targets.size());
        run.setTotalGross(totalGross);
        run.setTotalDeduction(totalDeduction);
        run.setTotalNet(totalNet);
        run.setExecutedBy(executor);
        run.setExecutedAt(LocalDateTime.now());

        auditLogRepository.save(AuditLog.builder()
                .actor(executor)
                .action("PAYROLL_CALCULATE")
                .targetType("PAYROLL_RUN")
                .targetId(String.valueOf(run.getId()))
                .detail("정산월=%s, 대상=%d명, 실지급합계=%s"
                        .formatted(run.getPayYearMonth(), targets.size(), totalNet))
                .build());

        log.info("급여 정산 완료 payYearMonth={} 대상={}명 실지급합계={}",
                run.getPayYearMonth(), targets.size(), totalNet);

        return PayrollRunRes.from(run);
    }

    @Transactional
    public PayrollRunRes confirm(Long runId, String executor) {
        PayrollRun run = getRunOrThrow(runId);
        if (run.isLocked()) {
            throw new BusinessException(ErrorCode.PAYROLL_ALREADY_CONFIRMED);
        }
        if (run.getStatus() != PayrollRun.Status.CALCULATED) {
            throw new BusinessException(ErrorCode.CONFLICT, "계산이 완료된 정산만 확정할 수 있습니다");
        }

        run.confirm(LocalDateTime.now());

        auditLogRepository.save(AuditLog.builder()
                .actor(executor)
                .action("PAYROLL_CONFIRM")
                .targetType("PAYROLL_RUN")
                .targetId(String.valueOf(run.getId()))
                .detail("정산월=%s".formatted(run.getPayYearMonth()))
                .build());

        return PayrollRunRes.from(run);
    }

    // MARK: 명세서 산출

    private Payslip buildPayslip(
            PayrollRun run,
            Employee employee,
            LocalDate from,
            LocalDate to,
            Map<String, PayItem> payItems,
            Map<String, DeductionRate> rates) {

        BigDecimal basePay = salaryContractRepository.findEffectiveContract(employee.getId(), to)
                .map(SalaryContract::getBasePay)
                .orElse(BigDecimal.ZERO);

        int workDays = (int) attendanceRepository.countByStatuses(
                employee.getId(), from, to, WORKED_STATUSES);

        long extendedMinutes = overtimeRequestRepository.sumApprovedMinutes(
                employee.getId(), from, to, ApprovalStatus.APPROVED, OvertimeRequest.OvertimeType.EXTENDED);
        long nightMinutes = overtimeRequestRepository.sumApprovedMinutes(
                employee.getId(), from, to, ApprovalStatus.APPROVED, OvertimeRequest.OvertimeType.NIGHT);
        long holidayMinutes = overtimeRequestRepository.sumApprovedMinutes(
                employee.getId(), from, to, ApprovalStatus.APPROVED, OvertimeRequest.OvertimeType.HOLIDAY);

        BigDecimal hourlyRate = PayrollPolicy.hourlyRate(basePay);

        Payslip payslip = Payslip.builder()
                .payrollRun(run)
                .employee(employee)
                .empNo(employee.getEmpNo())
                .employeeName(employee.getName())
                .departmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .positionName(employee.getJobPosition() != null ? employee.getJobPosition().getName() : null)
                .workDays(workDays)
                .overtimeMinutes((int) (extendedMinutes + nightMinutes + holidayMinutes))
                .build();

        Map<String, BigDecimal> earnings = new LinkedHashMap<>();
        earnings.put("BASE", PayrollPolicy.toWon(basePay));
        earnings.put("POSITION_ALLOWANCE", PayrollPolicy.positionAllowance(
                employee.getJobPosition() != null ? employee.getJobPosition().getLevelNo() : null));
        earnings.put("MEAL", PayrollPolicy.MEAL_ALLOWANCE);
        earnings.put("OVERTIME", allowance(hourlyRate, extendedMinutes, PayrollPolicy.RATE_EXTENDED));
        earnings.put("NIGHT", allowance(hourlyRate, nightMinutes, PayrollPolicy.RATE_NIGHT));
        earnings.put("HOLIDAY_WORK", allowance(hourlyRate, holidayMinutes, PayrollPolicy.RATE_HOLIDAY));

        BigDecimal grossPay = BigDecimal.ZERO;
        BigDecimal taxableBase = BigDecimal.ZERO;

        for (Map.Entry<String, BigDecimal> entry : earnings.entrySet()) {
            PayItem item = payItems.get(entry.getKey());
            if (item == null || entry.getValue().signum() <= 0) {
                continue;
            }
            BigDecimal amount = PayrollPolicy.toWon(entry.getValue());
            payslip.addDetail(newDetail(item, amount, null));

            grossPay = grossPay.add(amount);
            if (item.isTaxable()) {
                taxableBase = taxableBase.add(amount);
            }
        }

        BigDecimal totalDeduction = applyDeductions(payslip, payItems, rates, taxableBase);

        payslip.setGrossPay(grossPay);
        payslip.setTotalDeduction(totalDeduction);
        payslip.setNetPay(grossPay.subtract(totalDeduction));
        return payslip;
    }

    /**
     * 공제 적용
     *
     * NOTE: 장기요양보험은 급여가 아닌 건강보험료액 기준
     * NOTE: 지방소득세는 소득세액 기준
     */
    private BigDecimal applyDeductions(
            Payslip payslip,
            Map<String, PayItem> payItems,
            Map<String, DeductionRate> rates,
            BigDecimal taxableBase) {

        BigDecimal pension = rateAmount(rates, "NATIONAL_PENSION", taxableBase);
        BigDecimal health = rateAmount(rates, "HEALTH_INSURANCE", taxableBase);
        BigDecimal longTermCare = rateAmount(rates, "LONG_TERM_CARE", health);
        BigDecimal employment = rateAmount(rates, "EMPLOYMENT_INSURANCE", taxableBase);
        BigDecimal incomeTax = rateAmount(rates, "INCOME_TAX", taxableBase);
        BigDecimal localIncomeTax = rateAmount(rates, "LOCAL_INCOME_TAX", incomeTax);

        Map<String, BigDecimal> deductions = new LinkedHashMap<>();
        deductions.put("NATIONAL_PENSION", pension);
        deductions.put("HEALTH_INSURANCE", health);
        deductions.put("LONG_TERM_CARE", longTermCare);
        deductions.put("EMPLOYMENT_INSURANCE", employment);
        deductions.put("INCOME_TAX", incomeTax);
        deductions.put("LOCAL_INCOME_TAX", localIncomeTax);

        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<String, BigDecimal> entry : deductions.entrySet()) {
            PayItem item = payItems.get(entry.getKey());
            if (item == null || entry.getValue().signum() <= 0) {
                continue;
            }
            payslip.addDetail(newDetail(item, entry.getValue(), null));
            total = total.add(entry.getValue());
        }
        return total;
    }

    private BigDecimal rateAmount(Map<String, DeductionRate> rates, String itemCode, BigDecimal base) {
        DeductionRate rate = rates.get(itemCode);
        if (rate == null || base.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return PayrollPolicy.toWon(base.multiply(rate.getEmployeeRate()));
    }

    private BigDecimal allowance(BigDecimal hourlyRate, long minutes, BigDecimal multiplier) {
        if (minutes <= 0 || hourlyRate.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return PayrollPolicy.toWon(
                hourlyRate.multiply(PayrollPolicy.minutesToHours(minutes)).multiply(multiplier));
    }

    private PayslipDetail newDetail(PayItem item, BigDecimal amount, String note) {
        return PayslipDetail.builder()
                .payItem(item)
                .itemCode(item.getCode())
                .itemName(item.getName())
                .itemType(item.getItemType())
                .amount(amount)
                .note(note)
                .build();
    }

    private YearMonth parseYearMonth(String value) {
        try {
            return YearMonth.parse(value);
        } catch (RuntimeException e) {
            throw new BusinessException(ErrorCode.INVALID_YEAR_MONTH);
        }
    }

    private PayrollRun getRunOrThrow(Long id) {
        return payrollRunRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound(ErrorCode.PAYROLL_RUN_NOT_FOUND, id));
    }
}
