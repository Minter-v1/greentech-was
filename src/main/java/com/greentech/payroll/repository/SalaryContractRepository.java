package com.greentech.payroll.repository;

import com.greentech.payroll.domain.SalaryContract;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SalaryContractRepository extends JpaRepository<SalaryContract, Long> {

    List<SalaryContract> findByEmployeeIdOrderByEffectiveFromDesc(Long employeeId);

    // NOTE: 기준일에 유효한 계약 1건 - effective_to 가 NULL 이면 무기한 유효
    @Query("""
            select c from SalaryContract c
            where c.employee.id = :employeeId
              and c.effectiveFrom <= :baseDate
              and (c.effectiveTo is null or c.effectiveTo >= :baseDate)
            order by c.effectiveFrom desc
            limit 1
            """)
    Optional<SalaryContract> findEffectiveContract(
            @Param("employeeId") Long employeeId, @Param("baseDate") LocalDate baseDate);
}
