package com.greentech.employee.domain;

import com.greentech.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** 자격증. 스캔본은 attachment(owner_type=CERTIFICATE) 로 연결 */
@Entity
@Table(name = "certificate")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Certificate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String issuer;

    @Column(name = "license_no", length = 100)
    private String licenseNo;

    @Column(name = "acquired_date")
    private LocalDate acquiredDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;
}
