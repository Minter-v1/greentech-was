package com.greentech.payroll.domain;

import com.greentech.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * 연도별 공제 요율 - 4대보험·세율
 *
 * NOTE: 요율은 매년 변동 - 코드가 아닌 데이터로 관리
 */
@Entity
@Table(name = "deduction_rate")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeductionRate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int year;

    /** PayItem 중 DEDUCTION 항목 코드와 연결 */
    @Column(name = "item_code", nullable = false, length = 30)
    private String itemCode;

    @Column(name = "employee_rate", nullable = false, precision = 8, scale = 5)
    @Builder.Default
    private BigDecimal employeeRate = BigDecimal.ZERO;

    @Column(name = "employer_rate", nullable = false, precision = 8, scale = 5)
    @Builder.Default
    private BigDecimal employerRate = BigDecimal.ZERO;

    @Column(length = 200)
    private String description;
}
