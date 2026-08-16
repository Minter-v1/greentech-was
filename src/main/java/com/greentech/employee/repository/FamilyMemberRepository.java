package com.greentech.employee.repository;

import com.greentech.employee.domain.FamilyMember;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {

    List<FamilyMember> findByEmployeeIdOrderByIdAsc(Long employeeId);
}
