package com.greentech.org.repository;

import com.greentech.org.domain.Department;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByCode(String code);

    boolean existsByCode(String code);

    List<Department> findAllByOrderBySortOrderAscIdAsc();

    List<Department> findByActiveTrueOrderBySortOrderAscIdAsc();

    boolean existsByParentId(Long parentId);
}
