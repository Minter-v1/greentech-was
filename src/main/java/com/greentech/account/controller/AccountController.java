package com.greentech.account.controller;

import com.greentech.account.domain.AppRole;
import com.greentech.account.dto.req.AccountEmployeeLinkReq;
import com.greentech.account.dto.req.PasswordChangeReq;
import com.greentech.account.dto.res.AccountRes;
import com.greentech.account.service.AccountService;
import com.greentech.common.dto.res.ApiResult;
import com.greentech.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "10 계정", description = "계정 조회, 사원 연결, 비밀번호 변경")
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "계정 목록 조회")
    @PreAuthorize("hasAuthority('" + AppRole.ADMIN + "')")
    @GetMapping
    public ApiResult<List<AccountRes>> findAll() {
        return ApiResult.ok(accountService.findAll());
    }

    @Operation(summary = "계정에 사원 연결",
            description = "연결해야 토큰에 empId 가 실려 출퇴근 등 본인 대상 API 사용 가능. null 전송 시 연결 해제")
    @PreAuthorize("hasAuthority('" + AppRole.ADMIN + "')")
    @PatchMapping("/{userId}/employee")
    public ApiResult<AccountRes> linkEmployee(
            @PathVariable Long userId, @Valid @RequestBody AccountEmployeeLinkReq request) {
        return ApiResult.ok(accountService.linkEmployee(userId, request), "사원이 연결되었습니다");
    }

    @Operation(summary = "내 비밀번호 변경")
    @PatchMapping("/me/password")
    public ApiResult<Void> changePassword(@Valid @RequestBody PasswordChangeReq request) {
        accountService.changePassword(SecurityUtils.currentUsername(), request);
        return ApiResult.ok(null, "비밀번호가 변경되었습니다");
    }
}
