package com.greentech.employee.repository;

import com.greentech.employee.domain.EmploymentHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmploymentHistoryRepository extends JpaRepository<EmploymentHistory, Long> {

    List<EmploymentHistory> findByEmployeeIdOrderByEffectiveDateDescIdDesc(Long employeeId);
}
