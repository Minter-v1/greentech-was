package com.greentech.org.controller;

import com.greentech.account.domain.AppRole;
import com.greentech.common.dto.res.ApiResult;
import com.greentech.org.dto.req.JobPositionCreateReq;
import com.greentech.org.dto.req.JobPositionPatchReq;
import com.greentech.org.dto.res.JobPositionRes;
import com.greentech.org.service.JobPositionService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "03 조직 - 직위", description = "직위 등록 및 조회")
@RestController
@RequestMapping("/api/v1/positions")
@RequiredArgsConstructor
public class JobPositionController {

    private final JobPositionService jobPositionService;

    @Operation(summary = "직위 목록 조회", description = "직위 서열 오름차순 정렬")
    @GetMapping
    public ApiResult<List<JobPositionRes>> findAll(
            @Parameter(description = "사용 중인 직위만 조회")
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        return ApiResult.ok(jobPositionService.findAll(activeOnly));
    }

    @Operation(summary = "직위 등록")
    @PreAuthorize("hasAnyAuthority('" + AppRole.ADMIN + "', '" + AppRole.HR + "')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResult<JobPositionRes> create(@Valid @RequestBody JobPositionCreateReq request) {
        return ApiResult.ok(jobPositionService.create(request), "직위가 등록되었습니다");
    }

    @Operation(summary = "직위 부분 수정", description = "직위명, 서열, 사용 여부 변경")
    @PreAuthorize("hasAnyAuthority('" + AppRole.ADMIN + "', '" + AppRole.HR + "')")
    @PatchMapping("/{id}")
    public ApiResult<JobPositionRes> patch(
            @PathVariable Long id, @Valid @RequestBody JobPositionPatchReq request) {
        return ApiResult.ok(jobPositionService.patch(id, request), "직위가 수정되었습니다");
    }

    @Operation(summary = "직위 비활성화")
    @PreAuthorize("hasAnyAuthority('" + AppRole.ADMIN + "', '" + AppRole.HR + "')")
    @DeleteMapping("/{id}")
    public ApiResult<Void> deactivate(@PathVariable Long id) {
        jobPositionService.deactivate(id);
        return ApiResult.ok(null, "직위가 비활성화되었습니다");
    }
}
