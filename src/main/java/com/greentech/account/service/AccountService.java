package com.greentech.account.service;

import com.greentech.account.domain.AppUser;
import com.greentech.account.dto.req.AccountEmployeeLinkReq;
import com.greentech.account.dto.req.PasswordChangeReq;
import com.greentech.account.dto.res.AccountRes;
import com.greentech.account.repository.AppUserRepository;
import com.greentech.common.exception.BusinessException;
import com.greentech.common.exception.ErrorCode;
import com.greentech.employee.domain.Employee;
import com.greentech.employee.repository.EmployeeRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 계정 조회, 사원 연결, 비밀번호 변경 */
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AppUserRepository appUserRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<AccountRes> findAll() {
        return appUserRepository.findAllByOrderByUsernameAsc().stream()
                .map(AccountRes::from)
                .toList();
    }

    // NOTE: 사원 미연결 계정은 JWT 에 empId 가 없어 본인 대상 API 를 호출할 수 없다
    @Transactional
    public AccountRes linkEmployee(Long userId, AccountEmployeeLinkReq request) {
        AppUser user = appUserRepository.findWithDetailsById(userId)
                .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, userId));

        Long employeeId = (request.employeeId() != null && request.employeeId().isPresent())
                ? request.employeeId().get()
                : null;

        if (employeeId == null) {
            user.setEmployee(null);
            return AccountRes.from(user);
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> BusinessException.notFound(ErrorCode.EMPLOYEE_NOT_FOUND, employeeId));

        boolean alreadyLinkedToOther = appUserRepository.existsByEmployeeId(employeeId)
                && (user.getEmployee() == null || !employeeId.equals(user.getEmployee().getId()));
        if (alreadyLinkedToOther) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 다른 계정에 연결된 사원입니다");
        }

        user.setEmployee(employee);
        return AccountRes.from(user);
    }

    @Transactional
    public void changePassword(String username, PasswordChangeReq request) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.BAD_CREDENTIALS, "현재 비밀번호가 올바르지 않습니다");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "이전과 다른 비밀번호를 사용하세요");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
    }
}
