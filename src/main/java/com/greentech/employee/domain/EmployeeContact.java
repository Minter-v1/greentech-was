package com.greentech.employee.domain;

import com.greentech.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** 사원 연락처·주소·비상연락처. 사원당 1건 */
@Entity
@Table(name = "employee_contact")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmployeeContact extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    private Employee employee;

    @Column(length = 30)
    private String mobile;

    @Column(length = 30)
    private String tel;

    @Column(name = "zip_code", length = 10)
    private String zipCode;

    @Column(length = 200)
    private String address1;

    @Column(length = 200)
    private String address2;

    @Column(name = "emergency_name", length = 50)
    private String emergencyName;

    @Column(name = "emergency_relation", length = 30)
    private String emergencyRelation;

    @Column(name = "emergency_phone", length = 30)
    private String emergencyPhone;
}
