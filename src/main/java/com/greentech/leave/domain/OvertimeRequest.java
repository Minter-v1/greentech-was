package com.greentech.leave.domain;

import com.greentech.common.domain.BaseEntity;
import com.greentech.common.enums.ApprovalStatus;
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

/** 연장근무 신청. 승인 건만 급여 정산 수당 반영 */
@Entity
@Table(name = "overtime_request")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OvertimeRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false)
    private int minutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "overtime_type", nullable = false, length = 20)
    private OvertimeType overtimeType;

    @Column(length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ApprovalStatus status = ApprovalStatus.REQUESTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private Employee approver;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    public void approve(Employee approver, LocalDateTime at) {
        this.status = ApprovalStatus.APPROVED;
        this.approver = approver;
        this.approvedAt = at;
    }

    public void reject(Employee approver, LocalDateTime at) {
        this.status = ApprovalStatus.REJECTED;
        this.approver = approver;
        this.approvedAt = at;
    }

    /** 연장·야간·휴일 구분. 가산 배율 상이 */
    public enum OvertimeType {
        EXTENDED, NIGHT, HOLIDAY
    }
}
