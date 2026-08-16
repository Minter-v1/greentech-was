package com.greentech.employee.repository;

import com.greentech.employee.domain.EmployeeContact;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeContactRepository extends JpaRepository<EmployeeContact, Long> {

    Optional<EmployeeContact> findByEmployeeId(Long employeeId);
}
