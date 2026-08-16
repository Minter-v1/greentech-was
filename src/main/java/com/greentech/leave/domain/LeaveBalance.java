package com.greentech.leave.domain;

import com.greentech.common.domain.BaseEntity;
import com.greentech.employee.domain.Employee;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** 연도별 휴가 부여·사용 잔여. (employee, leaveType, year) 유니크 */
@Entity
@Table(name = "leave_balance")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LeaveBalance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(nullable = false)
    private int year;

    @Column(name = "granted_days", nullable = false, precision = 5, scale = 1)
    @Builder.Default
    private BigDecimal grantedDays = BigDecimal.ZERO;

    @Column(name = "used_days", nullable = false, precision = 5, scale = 1)
    @Builder.Default
    private BigDecimal usedDays = BigDecimal.ZERO;

    @Column(name = "expires_on")
    private LocalDate expiresOn;

    /** 잔여일수 비저장. 부여-사용 계산으로 불일치 방지 */
    public BigDecimal getRemainingDays() {
        return grantedDays.subtract(usedDays);
    }

    public void use(BigDecimal days) {
        this.usedDays = this.usedDays.add(days);
    }

    public void restore(BigDecimal days) {
        this.usedDays = this.usedDays.subtract(days);
    }
}
