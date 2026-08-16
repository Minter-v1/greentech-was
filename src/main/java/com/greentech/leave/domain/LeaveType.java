package com.greentech.leave.domain;

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

/** 휴가 종류. 연차·병가·경조사 등 */
@Entity
@Table(name = "leave_type")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LeaveType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20, unique = true)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    /** 유급 여부. 무급 휴가는 급여 일할 공제 대상 */
    @Column(nullable = false)
    @Builder.Default
    private boolean paid = true;

    /** 연차 잔여 차감 여부 */
    @Column(name = "deduct_annual", nullable = false)
    @Builder.Default
    private boolean deductAnnual = true;

    @Column(name = "max_days_per_year", precision = 5, scale = 1)
    private BigDecimal maxDaysPerYear;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
