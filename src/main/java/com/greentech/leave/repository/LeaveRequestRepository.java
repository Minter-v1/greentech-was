package com.greentech.leave.repository;

import com.greentech.common.enums.ApprovalStatus;
import com.greentech.leave.domain.LeaveRequest;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    @EntityGraph(attributePaths = {"employee", "leaveType"})
    Page<LeaveRequest> findByEmployeeIdOrderByIdDesc(Long employeeId, Pageable pageable);

    @EntityGraph(attributePaths = {"employee", "leaveType"})
    Page<LeaveRequest> findByStatusOrderByIdDesc(ApprovalStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"employee", "leaveType"})
    Page<LeaveRequest> findAllByOrderByIdDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"employee", "leaveType"})
    Page<LeaveRequest> findByEmployeeManagerIdOrderByIdDesc(Long managerId, Pageable pageable);

    @EntityGraph(attributePaths = {"employee", "leaveType"})
    Page<LeaveRequest> findByEmployeeManagerIdAndStatusOrderByIdDesc(
            Long managerId, ApprovalStatus status, Pageable pageable);

    @Query("""
            select count(r) from LeaveRequest r
            where r.employee.id = :employeeId
              and r.status in :statuses
              and r.startDate <= :endDate
              and r.endDate >= :startDate
            """)
    long countOverlapping(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") Collection<ApprovalStatus> statuses);

    @EntityGraph(attributePaths = {"leaveType"})
    List<LeaveRequest> findByEmployeeIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long employeeId, ApprovalStatus status, LocalDate endDate, LocalDate startDate);
}
