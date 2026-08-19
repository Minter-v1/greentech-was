package com.greentech.account.service;

import com.greentech.account.domain.AppRole;
import com.greentech.account.domain.AppUser;
import com.greentech.account.dto.req.AccountCreateReq;
import com.greentech.account.dto.req.AccountEmployeeLinkReq;
import com.greentech.account.dto.req.AccountPasswordResetReq;
import com.greentech.account.dto.req.AccountRolesUpdateReq;
import com.greentech.account.dto.req.AccountStatusUpdateReq;
import com.greentech.account.dto.req.PasswordChangeReq;
import com.greentech.account.dto.res.AccountRes;
import com.greentech.account.dto.res.RoleRes;
import com.greentech.account.repository.AppRoleRepository;
import com.greentech.account.repository.AppUserRepository;
import com.greentech.common.exception.BusinessException;
import com.greentech.common.exception.ErrorCode;
import com.greentech.employee.domain.Employee;
import com.greentech.employee.repository.EmployeeRepository;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 계정 발급, 권한·상태 관리, 비밀번호 변경 */
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AppUserRepository appUserRepository;
    private final AppRoleRepository appRoleRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<AccountRes> findAll() {
        return appUserRepository.findAllByOrderByUsernameAsc().stream()
                .map(AccountRes::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoleRes> findRoles() {
        return appRoleRepository.findAllByOrderByIdAsc().stream()
                .map(RoleRes::from)
                .toList();
    }

    @Transactional
    public AccountRes create(AccountCreateReq request) {
        if (appUserRepository.existsByUsername(request.username())) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 사용 중인 아이디입니다");
        }

        Employee employee = findAvailableEmployee(request.employeeId(), null);
        Set<AppRole> roles = resolveRoles(request.roleCodes());
        AppUser user = AppUser.builder()
                .username(request.username())
                .passwordHash(passwordEncoder.encode(request.temporaryPassword()))
                .employee(employee)
                .enabled(true)
                .locked(false)
                .passwordChangedAt(LocalDateTime.now())
                .roles(roles)
                .build();
        return AccountRes.from(appUserRepository.save(user));
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

        user.setEmployee(findAvailableEmployee(employeeId, user));
        return AccountRes.from(user);
    }

    @Transactional
    public AccountRes updateRoles(
            Long userId, AccountRolesUpdateReq request, String currentUsername) {
        AppUser user = findAccount(userId);
        rejectSelfChange(user, currentUsername, "자기 자신의 권한은 변경할 수 없습니다");
        user.setRoles(resolveRoles(request.roleCodes()));
        return AccountRes.from(user);
    }

    @Transactional
    public AccountRes updateStatus(
            Long userId, AccountStatusUpdateReq request, String currentUsername) {
        AppUser user = findAccount(userId);
        rejectSelfChange(user, currentUsername, "자기 자신의 상태는 변경할 수 없습니다");
        user.setEnabled(request.enabled());
        user.setLocked(request.locked());
        if (!request.locked()) user.setFailedLoginCount(0);
        return AccountRes.from(user);
    }

    @Transactional
    public void resetPassword(
            Long userId, AccountPasswordResetReq request, String currentUsername) {
        AppUser user = findAccount(userId);
        rejectSelfChange(user, currentUsername, "자기 자신의 비밀번호는 내 비밀번호 변경을 이용하세요");
        user.setPasswordHash(passwordEncoder.encode(request.temporaryPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setFailedLoginCount(0);
        user.setLocked(false);
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

    private AppUser findAccount(Long userId) {
        return appUserRepository.findWithDetailsById(userId)
                .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, userId));
    }

    private Set<AppRole> resolveRoles(Set<String> roleCodes) {
        List<AppRole> roles = appRoleRepository.findAllByCodeIn(roleCodes);
        if (roles.size() != roleCodes.size()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "존재하지 않는 권한이 포함되어 있습니다");
        }
        return new LinkedHashSet<>(roles);
    }

    private Employee findAvailableEmployee(Long employeeId, AppUser currentUser) {
        if (employeeId == null) return null;

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> BusinessException.notFound(ErrorCode.EMPLOYEE_NOT_FOUND, employeeId));
        boolean belongsToCurrentUser = currentUser != null
                && currentUser.getEmployee() != null
                && employeeId.equals(currentUser.getEmployee().getId());
        if (appUserRepository.existsByEmployeeId(employeeId) && !belongsToCurrentUser) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 다른 계정에 연결된 사원입니다");
        }
        return employee;
    }

    private void rejectSelfChange(AppUser user, String currentUsername, String message) {
        if (user.getUsername().equals(currentUsername)) {
            throw new BusinessException(ErrorCode.CONFLICT, message);
        }
    }
}
