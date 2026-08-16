package com.greentech.payroll.domain;

import com.greentech.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** 급여 명세서 항목별 금액. 항목코드·명은 마스터 변경 대비 스냅샷 저장 */
@Entity
@Table(name = "payslip_detail")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PayslipDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payslip_id", nullable = false)
    private Payslip payslip;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pay_item_id", nullable = false)
    private PayItem payItem;

    @Column(name = "item_code", nullable = false, length = 30)
    private String itemCode;

    @Column(name = "item_name", nullable = false, length = 50)
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 20)
    private PayItem.ItemType itemType;

    @Column(nullable = false, precision = 15, scale = 0)
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(length = 200)
    private String note;
}
