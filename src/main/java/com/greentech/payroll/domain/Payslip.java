package com.greentech.payroll.domain;

import com.greentech.common.domain.BaseEntity;
import com.greentech.employee.domain.Employee;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

// NOTE: 부서명, 직위명, 사번은 정산 시점 스냅샷. 조직 변동에도 과거 명세서 불변
@Entity
@Table(name = "payslip")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payslip extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payroll_run_id", nullable = false)
    private PayrollRun payrollRun;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "emp_no", nullable = false, length = 20)
    private String empNo;

    @Column(name = "employee_name", nullable = false, length = 50)
    private String employeeName;

    @Column(name = "department_name", length = 100)
    private String departmentName;

    @Column(name = "position_name", length = 50)
    private String positionName;

    @Column(name = "work_days", nullable = false)
    @Builder.Default
    private int workDays = 0;

    @Column(name = "overtime_minutes", nullable = false)
    @Builder.Default
    private int overtimeMinutes = 0;

    @Column(name = "gross_pay", nullable = false, precision = 15, scale = 0)
    @Builder.Default
    private BigDecimal grossPay = BigDecimal.ZERO;

    @Column(name = "total_deduction", nullable = false, precision = 15, scale = 0)
    @Builder.Default
    private BigDecimal totalDeduction = BigDecimal.ZERO;

    @Column(name = "net_pay", nullable = false, precision = 15, scale = 0)
    @Builder.Default
    private BigDecimal netPay = BigDecimal.ZERO;

    @OneToMany(mappedBy = "payslip", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PayslipDetail> details = new ArrayList<>();

    public void addDetail(PayslipDetail detail) {
        details.add(detail);
        detail.setPayslip(this);
    }
}
