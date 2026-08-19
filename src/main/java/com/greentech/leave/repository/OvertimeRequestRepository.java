package com.greentech.leave.repository;

import com.greentech.common.enums.ApprovalStatus;
import com.greentech.leave.domain.OvertimeRequest;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OvertimeRequestRepository extends JpaRepository<OvertimeRequest, Long> {

    @EntityGraph(attributePaths = {"employee"})
    Page<OvertimeRequest> findByEmployeeIdOrderByIdDesc(Long employeeId, Pageable pageable);

    @EntityGraph(attributePaths = {"employee"})
    Page<OvertimeRequest> findByStatusOrderByIdDesc(ApprovalStatus status, Pageable pageable);

    List<OvertimeRequest> findByEmployeeIdAndWorkDateBetweenAndStatus(
            Long employeeId, LocalDate from, LocalDate to, ApprovalStatus status);

    @Query("""
            select coalesce(sum(o.minutes), 0) from OvertimeRequest o
            where o.employee.id = :employeeId
              and o.workDate between :from and :to
              and o.status = :status
              and o.overtimeType = :type
            """)
    long sumApprovedMinutes(
            @Param("employeeId") Long employeeId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("status") ApprovalStatus status,
            @Param("type") OvertimeRequest.OvertimeType type);
}
