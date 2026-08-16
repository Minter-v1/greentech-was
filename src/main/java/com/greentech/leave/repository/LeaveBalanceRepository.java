package com.greentech.leave.repository;

import com.greentech.leave.domain.LeaveBalance;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    @EntityGraph(attributePaths = {"leaveType"})
    List<LeaveBalance> findByEmployeeIdAndYearOrderByIdAsc(Long employeeId, int year);

    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeIdAndYear(
            Long employeeId, Long leaveTypeId, int year);
}
