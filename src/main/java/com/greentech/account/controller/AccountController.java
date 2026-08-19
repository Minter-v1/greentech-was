package com.greentech.account.controller;

import com.greentech.account.domain.AppRole;
import com.greentech.account.dto.req.AccountCreateReq;
import com.greentech.account.dto.req.AccountEmployeeLinkReq;
import com.greentech.account.dto.req.AccountPasswordResetReq;
import com.greentech.account.dto.req.AccountRolesUpdateReq;
import com.greentech.account.dto.req.AccountStatusUpdateReq;
import com.greentech.account.dto.req.PasswordChangeReq;
import com.greentech.account.dto.res.AccountRes;
import com.greentech.account.dto.res.RoleRes;
import com.greentech.account.service.AccountService;
import com.greentech.common.dto.res.ApiResult;
import com.greentech.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "10 계정", description = "계정 발급, 권한·상태 관리, 비밀번호 변경")
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "권한 목록 조회")
    @PreAuthorize("hasAuthority('" + AppRole.ADMIN + "')")
    @GetMapping("/roles")
    public ApiResult<List<RoleRes>> findRoles() {
        return ApiResult.ok(accountService.findRoles());
    }

    @Operation(summary = "계정 목록 조회")
    @PreAuthorize("hasAuthority('" + AppRole.ADMIN + "')")
    @GetMapping
    public ApiResult<List<AccountRes>> findAll() {
        return ApiResult.ok(accountService.findAll());
    }

    @Operation(summary = "계정 발급", description = "임시 비밀번호와 권한을 지정해 로그인 계정을 생성")
    @PreAuthorize("hasAuthority('" + AppRole.ADMIN + "')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResult<AccountRes> create(@Valid @RequestBody AccountCreateReq request) {
        return ApiResult.ok(accountService.create(request), "계정이 발급되었습니다");
    }

    @Operation(summary = "계정에 사원 연결",
            description = "연결해야 토큰에 empId 가 실려 출퇴근 등 본인 대상 API 사용 가능. null 전송 시 연결 해제")
    @PreAuthorize("hasAuthority('" + AppRole.ADMIN + "')")
    @PatchMapping("/{userId}/employee")
    public ApiResult<AccountRes> linkEmployee(
            @PathVariable Long userId, @Valid @RequestBody AccountEmployeeLinkReq request) {
        return ApiResult.ok(accountService.linkEmployee(userId, request), "사원이 연결되었습니다");
    }

    @Operation(summary = "계정 권한 변경", description = "변경된 권한은 해당 사용자의 재로그인 후 적용")
    @PreAuthorize("hasAuthority('" + AppRole.ADMIN + "')")
    @PatchMapping("/{userId}/roles")
    public ApiResult<AccountRes> updateRoles(
            @PathVariable Long userId, @Valid @RequestBody AccountRolesUpdateReq request) {
        return ApiResult.ok(
                accountService.updateRoles(userId, request, SecurityUtils.currentUsername()),
                "권한이 변경되었습니다");
    }

    @Operation(summary = "계정 상태 변경", description = "비활성화·잠금 변경은 기존 토큰 만료 후 완전히 적용")
    @PreAuthorize("hasAuthority('" + AppRole.ADMIN + "')")
    @PatchMapping("/{userId}/status")
    public ApiResult<AccountRes> updateStatus(
            @PathVariable Long userId, @Valid @RequestBody AccountStatusUpdateReq request) {
        return ApiResult.ok(
                accountService.updateStatus(userId, request, SecurityUtils.currentUsername()),
                "계정 상태가 변경되었습니다");
    }

    @Operation(summary = "계정 임시 비밀번호 재설정")
    @PreAuthorize("hasAuthority('" + AppRole.ADMIN + "')")
    @PatchMapping("/{userId}/password")
    public ApiResult<Void> resetPassword(
            @PathVariable Long userId, @Valid @RequestBody AccountPasswordResetReq request) {
        accountService.resetPassword(userId, request, SecurityUtils.currentUsername());
        return ApiResult.ok(null, "임시 비밀번호가 설정되었습니다");
    }

    @Operation(summary = "내 비밀번호 변경")
    @PatchMapping("/me/password")
    public ApiResult<Void> changePassword(@Valid @RequestBody PasswordChangeReq request) {
        accountService.changePassword(SecurityUtils.currentUsername(), request);
        return ApiResult.ok(null, "비밀번호가 변경되었습니다");
    }
}
