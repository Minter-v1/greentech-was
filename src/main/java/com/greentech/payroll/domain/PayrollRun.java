package com.greentech.payroll.domain;

import com.greentech.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** 월별 급여 정산 실행 단위. CONFIRMED 이후 잠금 */
@Entity
@Table(name = "payroll_run")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PayrollRun extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** YYYY-MM */
    @Column(name = "pay_year_month", nullable = false, length = 7, unique = true)
    private String payYearMonth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.DRAFT;

    @Column(name = "pay_date")
    private LocalDate payDate;

    @Column(name = "target_count", nullable = false)
    @Builder.Default
    private int targetCount = 0;

    @Column(name = "total_gross", nullable = false, precision = 18, scale = 0)
    @Builder.Default
    private BigDecimal totalGross = BigDecimal.ZERO;

    @Column(name = "total_deduction", nullable = false, precision = 18, scale = 0)
    @Builder.Default
    private BigDecimal totalDeduction = BigDecimal.ZERO;

    @Column(name = "total_net", nullable = false, precision = 18, scale = 0)
    @Builder.Default
    private BigDecimal totalNet = BigDecimal.ZERO;

    @Column(name = "executed_by", length = 50)
    private String executedBy;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    public boolean isLocked() {
        return status == Status.CONFIRMED;
    }

    public void confirm(LocalDateTime at) {
        this.status = Status.CONFIRMED;
        this.confirmedAt = at;
    }

    public enum Status {
        DRAFT, CALCULATED, CONFIRMED, CANCELED
    }
}
