package com.greentech.payroll.repository;

import com.greentech.payroll.domain.PayrollRun;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollRunRepository extends JpaRepository<PayrollRun, Long> {

    Optional<PayrollRun> findByPayYearMonth(String payYearMonth);

    Page<PayrollRun> findAllByOrderByPayYearMonthDesc(Pageable pageable);
}
