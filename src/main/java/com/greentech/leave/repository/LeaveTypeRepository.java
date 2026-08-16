package com.greentech.leave.repository;

import com.greentech.leave.domain.LeaveType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {

    Optional<LeaveType> findByCode(String code);

    List<LeaveType> findByActiveTrueOrderByIdAsc();
}
