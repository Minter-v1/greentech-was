package com.greentech.auth.controller;

import com.greentech.auth.dto.req.LoginReq;
import com.greentech.auth.dto.res.LoginRes;
import com.greentech.auth.dto.res.MeRes;
import com.greentech.auth.service.AuthService;
import com.greentech.common.dto.res.ApiResult;
import com.greentech.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "01 인증", description = "로그인 및 토큰 발급")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String HEADER_FORWARDED_FOR = "X-Forwarded-For";

    private final AuthService authService;

    @Operation(summary = "로그인", description = "계정 아이디와 비밀번호로 액세스 토큰 발급 - 인증 불필요")
    @ApiResponse(responseCode = "200", description = "발급 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @SecurityRequirements
    @PostMapping("/login")
    public ApiResult<LoginRes> login(
            @Valid @RequestBody LoginReq request, HttpServletRequest servletRequest) {
        LoginRes response = authService.login(request, resolveClientIp(servletRequest));
        return ApiResult.ok(response, "로그인되었습니다");
    }

    @Operation(summary = "내 정보 조회", description = "토큰에 연결된 계정과 사원 정보 조회")
    @GetMapping("/me")
    public ApiResult<MeRes> me() {
        return ApiResult.ok(authService.me(SecurityUtils.currentUsername()));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader(HEADER_FORWARDED_FOR);
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
