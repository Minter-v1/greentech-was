package com.greentech.employee.controller;

import com.greentech.account.domain.AppRole;
import com.greentech.common.dto.res.ApiResult;
import com.greentech.common.dto.res.PageResult;
import com.greentech.employee.domain.Employee;
import com.greentech.employee.dto.req.CertificateCreateReq;
import com.greentech.employee.dto.req.EducationCreateReq;
import com.greentech.employee.dto.req.EmployeeContactUpsertReq;
import com.greentech.employee.dto.req.EmployeeCreateReq;
import com.greentech.employee.dto.req.EmployeeResignReq;
import com.greentech.employee.dto.req.EmployeeUpdateReq;
import com.greentech.employee.dto.req.FamilyMemberCreateReq;
import com.greentech.employee.dto.res.CertificateRes;
import com.greentech.employee.dto.res.EducationRes;
import com.greentech.employee.dto.res.EmployeeContactRes;
import com.greentech.employee.dto.res.EmployeeDetailRes;
import com.greentech.employee.dto.res.EmployeeSummaryRes;
import com.greentech.employee.dto.res.EmploymentHistoryRes;
import com.greentech.employee.dto.res.FamilyMemberRes;
import com.greentech.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "04 사원", description = "사원 기본정보 및 학력·자격증·가족사항 관리")
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private static final String HR_OR_ADMIN =
            "hasAnyAuthority('" + AppRole.ADMIN + "', '" + AppRole.HR + "')";

    private final EmployeeService employeeService;

    @Operation(summary = "사원 검색", description = "사번·성명·이메일 부분 일치 검색과 부서·재직상태 필터 지원")
    @GetMapping
    public ApiResult<PageResult<EmployeeSummaryRes>> search(
            @Parameter(description = "검색어 - 사번, 성명, 이메일") @RequestParam(required = false) String keyword,
            @Parameter(description = "부서 ID") @RequestParam(required = false) Long departmentId,
            @Parameter(description = "재직 상태") @RequestParam(required = false) Employee.Status status,
            @PageableDefault(size = 20, sort = "empNo", direction = Sort.Direction.ASC) Pageable pageable) {
        return ApiResult.ok(employeeService.search(keyword, departmentId, status, pageable));
    }

    @Operation(summary = "사원 상세 조회", description = "주민등록번호는 마스킹 값으로만 반환")
    @GetMapping("/{id}")
    public ApiResult<EmployeeDetailRes> findDetail(@PathVariable Long id) {
        return ApiResult.ok(employeeService.findDetail(id));
    }

    @Operation(summary = "사원 등록", description = "등록과 동시에 HIRE 발령 이력 생성")
    @PreAuthorize(HR_OR_ADMIN)
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResult<EmployeeDetailRes> create(@Valid @RequestBody EmployeeCreateReq request) {
        return ApiResult.ok(employeeService.create(request), "사원이 등록되었습니다");
    }

    @Operation(summary = "사원 수정", description = "부서·직위 변경 시 발령 이력 자동 기록")
    @PreAuthorize(HR_OR_ADMIN)
    @PutMapping("/{id}")
    public ApiResult<EmployeeDetailRes> update(
            @PathVariable Long id, @Valid @RequestBody EmployeeUpdateReq request) {
        return ApiResult.ok(employeeService.update(id, request), "사원 정보가 수정되었습니다");
    }

    @Operation(summary = "퇴사 처리", description = "재직 상태와 퇴사일을 함께 변경하고 RESIGN 이력 기록")
    @PreAuthorize(HR_OR_ADMIN)
    @PatchMapping("/{id}/resign")
    public ApiResult<EmployeeDetailRes> resign(
            @PathVariable Long id, @Valid @RequestBody EmployeeResignReq request) {
        return ApiResult.ok(employeeService.resign(id, request), "퇴사 처리되었습니다");
    }

    @Operation(summary = "발령 이력 조회")
    @GetMapping("/{id}/histories")
    public ApiResult<List<EmploymentHistoryRes>> findHistories(@PathVariable Long id) {
        return ApiResult.ok(employeeService.findHistories(id));
    }

    // MARK: 연락처

    @Operation(summary = "연락처 조회")
    @GetMapping("/{id}/contact")
    public ApiResult<EmployeeContactRes> findContact(@PathVariable Long id) {
        return ApiResult.ok(employeeService.findContact(id));
    }

    @Operation(summary = "연락처 등록·수정", description = "사원당 1건이며 없으면 생성, 있으면 갱신")
    @PreAuthorize(HR_OR_ADMIN)
    @PutMapping("/{id}/contact")
    public ApiResult<EmployeeContactRes> upsertContact(
            @PathVariable Long id, @Valid @RequestBody EmployeeContactUpsertReq request) {
        return ApiResult.ok(employeeService.upsertContact(id, request), "연락처가 저장되었습니다");
    }

    // MARK: 학력

    @Operation(summary = "학력 목록 조회")
    @GetMapping("/{id}/educations")
    public ApiResult<List<EducationRes>> findEducations(@PathVariable Long id) {
        return ApiResult.ok(employeeService.findEducations(id));
    }

    @Operation(summary = "학력 등록")
    @PreAuthorize(HR_OR_ADMIN)
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{id}/educations")
    public ApiResult<EducationRes> addEducation(
            @PathVariable Long id, @Valid @RequestBody EducationCreateReq request) {
        return ApiResult.ok(employeeService.addEducation(id, request), "학력이 등록되었습니다");
    }

    @Operation(summary = "학력 삭제")
    @PreAuthorize(HR_OR_ADMIN)
    @DeleteMapping("/educations/{educationId}")
    public ApiResult<Void> deleteEducation(@PathVariable Long educationId) {
        employeeService.deleteEducation(educationId);
        return ApiResult.ok(null, "학력이 삭제되었습니다");
    }

    // MARK: 자격증

    @Operation(summary = "자격증 목록 조회")
    @GetMapping("/{id}/certificates")
    public ApiResult<List<CertificateRes>> findCertificates(@PathVariable Long id) {
        return ApiResult.ok(employeeService.findCertificates(id));
    }

    @Operation(summary = "자격증 등록")
    @PreAuthorize(HR_OR_ADMIN)
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{id}/certificates")
    public ApiResult<CertificateRes> addCertificate(
            @PathVariable Long id, @Valid @RequestBody CertificateCreateReq request) {
        return ApiResult.ok(employeeService.addCertificate(id, request), "자격증이 등록되었습니다");
    }

    @Operation(summary = "자격증 삭제")
    @PreAuthorize(HR_OR_ADMIN)
    @DeleteMapping("/certificates/{certificateId}")
    public ApiResult<Void> deleteCertificate(@PathVariable Long certificateId) {
        employeeService.deleteCertificate(certificateId);
        return ApiResult.ok(null, "자격증이 삭제되었습니다");
    }

    // MARK: 가족사항

    @Operation(summary = "가족사항 목록 조회")
    @PreAuthorize(HR_OR_ADMIN)
    @GetMapping("/{id}/family-members")
    public ApiResult<List<FamilyMemberRes>> findFamilyMembers(@PathVariable Long id) {
        return ApiResult.ok(employeeService.findFamilyMembers(id));
    }

    @Operation(summary = "가족사항 등록", description = "부양가족 여부는 연말정산 산출 입력값")
    @PreAuthorize(HR_OR_ADMIN)
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{id}/family-members")
    public ApiResult<FamilyMemberRes> addFamilyMember(
            @PathVariable Long id, @Valid @RequestBody FamilyMemberCreateReq request) {
        return ApiResult.ok(employeeService.addFamilyMember(id, request), "가족사항이 등록되었습니다");
    }

    @Operation(summary = "가족사항 삭제")
    @PreAuthorize(HR_OR_ADMIN)
    @DeleteMapping("/family-members/{familyMemberId}")
    public ApiResult<Void> deleteFamilyMember(@PathVariable Long familyMemberId) {
        employeeService.deleteFamilyMember(familyMemberId);
        return ApiResult.ok(null, "가족사항이 삭제되었습니다");
    }
}
