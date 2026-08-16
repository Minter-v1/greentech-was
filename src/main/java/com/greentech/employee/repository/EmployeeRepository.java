package com.greentech.employee.repository;

import com.greentech.employee.domain.Employee;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmpNo(String empNo);

    boolean existsByEmpNo(String empNo);

    @EntityGraph(attributePaths = {"department", "jobPosition"})
    Optional<Employee> findWithOrgById(Long id);

    // NOTE: ToOne 연관만 fetch 하므로 페이징과 함께 써도 메모리 페이징 발생 없음
    @EntityGraph(attributePaths = {"department", "jobPosition"})
    @Query("""
            select e from Employee e
            where (:keyword is null or e.name like concat('%', :keyword, '%')
                   or e.empNo like concat('%', :keyword, '%')
                   or e.email like concat('%', :keyword, '%'))
              and (:departmentId is null or e.department.id = :departmentId)
              and (:status is null or e.status = :status)
            """)
    Page<Employee> search(
            @Param("keyword") String keyword,
            @Param("departmentId") Long departmentId,
            @Param("status") Employee.Status status,
            Pageable pageable);

    // NOTE: 급여 정산 대상 조회 - 정산월 기준 재직자만 포함
    @EntityGraph(attributePaths = {"department", "jobPosition"})
    List<Employee> findByStatusOrderByEmpNoAsc(Employee.Status status);

    long countByStatus(Employee.Status status);
}
