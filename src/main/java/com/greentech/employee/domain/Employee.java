package com.greentech.employee.domain;

import com.greentech.common.converter.EncryptedStringConverter;
import com.greentech.common.domain.BaseEntity;
import com.greentech.org.domain.Department;
import com.greentech.org.domain.JobPosition;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** 사원. 인사관리시스템 중심 엔티티 */
@Entity
@Table(name = "employee")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Employee extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "emp_no", nullable = false, length = 20, unique = true)
    private String empNo;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "name_en", length = 100)
    private String nameEn;

    /** 주민등록번호. AES-GCM 암호화 저장 */
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "resident_no_enc", length = 512)
    private String residentNo;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Column(length = 120)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_position_id")
    private JobPosition jobPosition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employee manager;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false, length = 20)
    private EmploymentType employmentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "resign_date")
    private LocalDate resignDate;

    /** 퇴사 처리. 상태·퇴사일 동시 변경으로 불일치 방지 */
    public void resign(LocalDate date) {
        this.status = Status.RESIGNED;
        this.resignDate = date;
    }

    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    public enum EmploymentType {
        FULL_TIME, CONTRACT, PART_TIME, DISPATCH
    }

    public enum Status {
        ACTIVE, ON_LEAVE, RESIGNED
    }
}
