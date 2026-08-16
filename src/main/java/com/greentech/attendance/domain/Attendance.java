package com.greentech.attendance.domain;

import com.greentech.common.domain.BaseEntity;
import com.greentech.employee.domain.Employee;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** 일별 출퇴근 기록. (employee_id, work_date) 유니크 */
@Entity
@Table(name = "attendance")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attendance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "check_in_at")
    private LocalDateTime checkInAt;

    @Column(name = "check_out_at")
    private LocalDateTime checkOutAt;

    @Column(name = "work_minutes", nullable = false)
    @Builder.Default
    private int workMinutes = 0;

    @Column(name = "overtime_minutes", nullable = false)
    @Builder.Default
    private int overtimeMinutes = 0;

    @Column(name = "night_minutes", nullable = false)
    @Builder.Default
    private int nightMinutes = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(length = 255)
    private String note;

    public enum Status {
        NORMAL, LATE, EARLY_LEAVE, ABSENT, ON_LEAVE, HOLIDAY
    }
}
