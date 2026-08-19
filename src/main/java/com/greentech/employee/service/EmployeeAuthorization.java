package com.greentech.employee.service;

import com.greentech.security.SecurityUtils;
import com.greentech.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 사원 본인 정보 조회 권한 */
@Component
@RequiredArgsConstructor
public class EmployeeAuthorization {

    private final EmployeeRepository employeeRepository;

    public boolean canRead(Long employeeId) {
        Long currentEmployeeId = SecurityUtils.currentEmployeeIdOrNull();
        return currentEmployeeId != null && currentEmployeeId.equals(employeeId);
    }

    public boolean isManagerOf(Long employeeId) {
        Long managerId = SecurityUtils.currentEmployeeIdOrNull();
        return managerId != null && employeeRepository.existsByIdAndManagerId(employeeId, managerId);
    }
}
