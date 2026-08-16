package com.greentech.employee.repository;

import com.greentech.employee.domain.Certificate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    List<Certificate> findByEmployeeIdOrderByAcquiredDateDesc(Long employeeId);
}
