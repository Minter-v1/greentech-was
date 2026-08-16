package com.greentech.payroll.repository;

import com.greentech.payroll.domain.DeductionRate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeductionRateRepository extends JpaRepository<DeductionRate, Long> {

    List<DeductionRate> findByYear(int year);

    Optional<DeductionRate> findByYearAndItemCode(int year, String itemCode);
}
