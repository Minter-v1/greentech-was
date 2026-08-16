package com.greentech.attendance.repository;

import com.greentech.attendance.domain.Attendance;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);

    List<Attendance> findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(
            Long employeeId, LocalDate from, LocalDate to);

    // NOTE: enum 리터럴을 JPQL 에 직접 쓰지 않고 파라미터로 전달
    @Query("""
            select count(a) from Attendance a
            where a.employee.id = :employeeId
              and a.workDate between :from and :to
              and a.status in :statuses
            """)
    long countByStatuses(
            @Param("employeeId") Long employeeId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("statuses") Collection<Attendance.Status> statuses);

    @Query("""
            select coalesce(sum(a.overtimeMinutes), 0) from Attendance a
            where a.employee.id = :employeeId
              and a.workDate between :from and :to
            """)
    long sumOvertimeMinutes(
            @Param("employeeId") Long employeeId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
