package com.greentech.payroll.repository;

import com.greentech.payroll.domain.Payslip;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayslipRepository extends JpaRepository<Payslip, Long> {

    List<Payslip> findByPayrollRunIdOrderByEmpNoAsc(Long payrollRunId);

    @EntityGraph(attributePaths = {"details", "payrollRun"})
    Optional<Payslip> findWithDetailsById(Long id);

    Optional<Payslip> findByPayrollRunIdAndEmployeeId(Long payrollRunId, Long employeeId);

    @EntityGraph(attributePaths = {"payrollRun"})
    List<Payslip> findByEmployeeIdOrderByIdDesc(Long employeeId);
}
