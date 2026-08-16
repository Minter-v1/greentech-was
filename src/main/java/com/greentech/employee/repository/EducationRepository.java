package com.greentech.employee.repository;

import com.greentech.employee.domain.Education;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EducationRepository extends JpaRepository<Education, Long> {

    List<Education> findByEmployeeIdOrderByGraduationDateDesc(Long employeeId);
}
