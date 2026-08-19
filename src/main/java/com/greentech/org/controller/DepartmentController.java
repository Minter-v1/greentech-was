package com.greentech.org.controller;

import com.greentech.account.domain.AppRole;
import com.greentech.common.dto.res.ApiResult;
import com.greentech.org.dto.req.DepartmentCreateReq;
import com.greentech.org.dto.req.DepartmentPatchReq;
import com.greentech.org.dto.res.DepartmentRes;
import com.greentech.org.dto.res.DepartmentTreeRes;
import com.greentech.org.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "02 조직 - 부서", description = "부서 등록 및 계층 조회")
@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @Operation(summary = "부서 목록 조회")
    @GetMapping
    public ApiResult<List<DepartmentRes>> findAll(
            @Parameter(description = "사용 중인 부서만 조회")
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        return ApiResult.ok(departmentService.findAll(activeOnly));
    }

    @Operation(summary = "부서 계층 구조 조회", description = "상위·하위 관계를 트리 형태로 반환")
    @GetMapping("/tree")
    public ApiResult<List<DepartmentTreeRes>> findTree() {
        return ApiResult.ok(departmentService.findTree());
    }

    @Operation(summary = "부서 단건 조회")
    @GetMapping("/{id}")
    public ApiResult<DepartmentRes> findById(@PathVariable Long id) {
        return ApiResult.ok(departmentService.findById(id));
    }

    @Operation(summary = "부서 등록")
    @PreAuthorize("hasAnyAuthority('" + AppRole.ADMIN + "', '" + AppRole.HR + "')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResult<DepartmentRes> create(@Valid @RequestBody DepartmentCreateReq request) {
        return ApiResult.ok(departmentService.create(request), "부서가 등록되었습니다");
    }

    @Operation(summary = "부서 부분 수정", description = "전송한 필드만 반영")
    @PreAuthorize("hasAnyAuthority('" + AppRole.ADMIN + "', '" + AppRole.HR + "')")
    @PatchMapping("/{id}")
    public ApiResult<DepartmentRes> patch(
            @PathVariable Long id, @Valid @RequestBody DepartmentPatchReq request) {
        return ApiResult.ok(departmentService.patch(id, request), "부서가 수정되었습니다");
    }

    @Operation(summary = "부서 비활성화", description = "참조 무결성 유지 목적으로 물리 삭제 대신 비활성화 처리")
    @PreAuthorize("hasAnyAuthority('" + AppRole.ADMIN + "', '" + AppRole.HR + "')")
    @DeleteMapping("/{id}")
    public ApiResult<Void> deactivate(@PathVariable Long id) {
        departmentService.deactivate(id);
        return ApiResult.ok(null, "부서가 비활성화되었습니다");
    }
}
