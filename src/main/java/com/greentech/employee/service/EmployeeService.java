package com.greentech.employee.service;

import com.greentech.common.dto.res.PageResult;
import com.greentech.common.exception.BusinessException;
import com.greentech.common.exception.ErrorCode;
import com.greentech.employee.domain.Certificate;
import com.greentech.employee.domain.Education;
import com.greentech.employee.domain.Employee;
import com.greentech.employee.domain.EmployeeContact;
import com.greentech.employee.domain.EmploymentHistory;
import com.greentech.employee.domain.FamilyMember;
import com.greentech.employee.dto.req.CertificateCreateReq;
import com.greentech.employee.dto.req.EducationCreateReq;
import com.greentech.employee.dto.req.EmployeeContactUpsertReq;
import com.greentech.employee.dto.req.EmployeeCreateReq;
import com.greentech.employee.dto.req.EmployeeResignReq;
import com.greentech.employee.dto.req.EmployeePatchReq;
import com.greentech.employee.dto.req.EmployeeStatusChangeReq;
import com.greentech.employee.dto.req.FamilyMemberCreateReq;
import com.greentech.employee.dto.res.CertificateRes;
import com.greentech.employee.dto.res.EducationRes;
import com.greentech.employee.dto.res.EmployeeContactRes;
import com.greentech.employee.dto.res.EmployeeDetailRes;
import com.greentech.employee.dto.res.EmployeeSummaryRes;
import com.greentech.employee.dto.res.EmploymentHistoryRes;
import com.greentech.employee.dto.res.FamilyMemberRes;
import com.greentech.employee.repository.CertificateRepository;
import com.greentech.employee.repository.EducationRepository;
import com.greentech.employee.repository.EmployeeContactRepository;
import com.greentech.employee.repository.EmployeeRepository;
import com.greentech.employee.repository.EmploymentHistoryRepository;
import com.greentech.employee.repository.FamilyMemberRepository;
import com.greentech.org.domain.Department;
import com.greentech.org.domain.JobPosition;
import com.greentech.org.repository.DepartmentRepository;
import com.greentech.org.repository.JobPositionRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.openapitools.jackson.nullable.JsonNullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 사원 기본정보 및 부속정보 관리 */
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeContactRepository employeeContactRepository;
    private final EducationRepository educationRepository;
    private final CertificateRepository certificateRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final EmploymentHistoryRepository employmentHistoryRepository;
    private final DepartmentRepository departmentRepository;
    private final JobPositionRepository jobPositionRepository;

    @Transactional(readOnly = true)
    public PageResult<EmployeeSummaryRes> search(
            String keyword, Long departmentId, Employee.Status status, Pageable pageable) {
        String normalized = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        Page<Employee> page = employeeRepository.search(normalized, departmentId, status, pageable);
        return PageResult.of(page, EmployeeSummaryRes::from);
    }

    @Transactional(readOnly = true)
    public EmployeeDetailRes findDetail(Long id) {
        return EmployeeDetailRes.from(getWithOrgOrThrow(id));
    }

    @Transactional
    public EmployeeDetailRes create(EmployeeCreateReq request) {
        if (employeeRepository.existsByEmpNo(request.empNo())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMP_NO, "이미 사용 중인 사번입니다: " + request.empNo());
        }

        Employee employee = Employee.builder()
                .empNo(request.empNo())
                .name(request.name())
                .nameEn(request.nameEn())
                .residentNo(emptyToNull(request.residentNo()))
                .birthDate(request.birthDate())
                .gender(request.gender())
                .email(request.email())
                .department(resolveDepartment(request.departmentId()))
                .jobPosition(resolvePosition(request.jobPositionId()))
                .manager(resolveManager(request.managerId()))
                .employmentType(request.employmentType())
                .status(Employee.Status.ACTIVE)
                .hireDate(request.hireDate())
                .build();

        Employee saved = employeeRepository.save(employee);

        employmentHistoryRepository.save(EmploymentHistory.builder()
                .employee(saved)
                .changeType(EmploymentHistory.ChangeType.HIRE)
                .effectiveDate(saved.getHireDate())
                .afterDepartmentId(saved.getDepartment() != null ? saved.getDepartment().getId() : null)
                .afterPositionId(saved.getJobPosition() != null ? saved.getJobPosition().getId() : null)
                .reason("신규 입사")
                .build());

        return EmployeeDetailRes.from(saved);
    }

    /** 사원 부분 수정. 전송한 필드만 반영하고 부서나 직위가 바뀌면 발령 이력 기록 */
    @Transactional
    public EmployeeDetailRes patch(Long id, EmployeePatchReq request) {
        Employee employee = getWithOrgOrThrow(id);

        Long beforeDepartmentId = idOf(employee.getDepartment());
        Long beforePositionId = idOf(employee.getJobPosition());

        apply(request.name(), employee::setName);
        apply(request.nameEn(), employee::setNameEn);
        apply(request.birthDate(), employee::setBirthDate);
        apply(request.gender(), employee::setGender);
        apply(request.email(), employee::setEmail);
        apply(request.employmentType(), employee::setEmploymentType);
        apply(request.departmentId(), value -> employee.setDepartment(resolveDepartment(value)));
        apply(request.jobPositionId(), value -> employee.setJobPosition(resolvePosition(value)));
        apply(request.managerId(), value -> employee.setManager(resolveManager(value)));

        recordOrgChange(employee, beforeDepartmentId, beforePositionId, unwrap(request.reason()));
        return EmployeeDetailRes.from(employee);
    }

    @Transactional
    public EmployeeDetailRes reinstate(Long id, EmployeeStatusChangeReq request) {
        Employee employee = getWithOrgOrThrow(id);
        if (employee.getStatus() == Employee.Status.ACTIVE) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 재직 중인 사원입니다");
        }

        employee.reinstate();
        saveHistory(employee, EmploymentHistory.ChangeType.REINSTATE, request.effectiveDate(),
                request.reason() != null ? request.reason() : "복직");
        return EmployeeDetailRes.from(employee);
    }

    @Transactional
    public EmployeeDetailRes takeLeaveOfAbsence(Long id, EmployeeStatusChangeReq request) {
        Employee employee = getWithOrgOrThrow(id);
        if (employee.getStatus() != Employee.Status.ACTIVE) {
            throw new BusinessException(ErrorCode.CONFLICT, "재직 중인 사원만 휴직 처리할 수 있습니다");
        }

        employee.takeLeaveOfAbsence();
        saveHistory(employee, EmploymentHistory.ChangeType.LEAVE_OF_ABSENCE, request.effectiveDate(),
                request.reason() != null ? request.reason() : "휴직");
        return EmployeeDetailRes.from(employee);
    }

    @Transactional
    public EmployeeDetailRes resign(Long id, EmployeeResignReq request) {
        Employee employee = getWithOrgOrThrow(id);
        if (employee.getStatus() == Employee.Status.RESIGNED) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 퇴사 처리된 사원입니다");
        }

        employee.resign(request.resignDate());

        employmentHistoryRepository.save(EmploymentHistory.builder()
                .employee(employee)
                .changeType(EmploymentHistory.ChangeType.RESIGN)
                .effectiveDate(request.resignDate())
                .beforeDepartmentId(employee.getDepartment() != null ? employee.getDepartment().getId() : null)
                .beforePositionId(employee.getJobPosition() != null ? employee.getJobPosition().getId() : null)
                .reason(request.reason())
                .build());

        return EmployeeDetailRes.from(employee);
    }

    // MARK: 연락처

    @Transactional(readOnly = true)
    public EmployeeContactRes findContact(Long employeeId) {
        return employeeContactRepository.findByEmployeeId(employeeId)
                .map(EmployeeContactRes::from)
                .orElseThrow(() -> BusinessException.notFound(ErrorCode.NOT_FOUND, employeeId));
    }

    @Transactional
    public EmployeeContactRes upsertContact(Long employeeId, EmployeeContactUpsertReq request) {
        Employee employee = getOrThrow(employeeId);
        EmployeeContact contact = employeeContactRepository.findByEmployeeId(employeeId)
                .orElseGet(() -> employeeContactRepository.save(
                        EmployeeContact.builder().employee(employee).build()));

        contact.setMobile(request.mobile());
        contact.setTel(request.tel());
        contact.setZipCode(request.zipCode());
        contact.setAddress1(request.address1());
        contact.setAddress2(request.address2());
        contact.setEmergencyName(request.emergencyName());
        contact.setEmergencyRelation(request.emergencyRelation());
        contact.setEmergencyPhone(request.emergencyPhone());

        return EmployeeContactRes.from(contact);
    }

    // MARK: 학력, 자격증, 가족사항

    @Transactional(readOnly = true)
    public List<EducationRes> findEducations(Long employeeId) {
        return educationRepository.findByEmployeeIdOrderByGraduationDateDesc(employeeId).stream()
                .map(EducationRes::from)
                .toList();
    }

    @Transactional
    public EducationRes addEducation(Long employeeId, EducationCreateReq request) {
        Employee employee = getOrThrow(employeeId);
        Education education = Education.builder()
                .employee(employee)
                .schoolName(request.schoolName())
                .major(request.major())
                .degree(request.degree())
                .admissionDate(request.admissionDate())
                .graduationDate(request.graduationDate())
                .graduated(request.graduated() == null || request.graduated())
                .build();
        return EducationRes.from(educationRepository.save(education));
    }

    @Transactional
    public void deleteEducation(Long educationId) {
        educationRepository.deleteById(educationId);
    }

    @Transactional(readOnly = true)
    public List<CertificateRes> findCertificates(Long employeeId) {
        return certificateRepository.findByEmployeeIdOrderByAcquiredDateDesc(employeeId).stream()
                .map(CertificateRes::from)
                .toList();
    }

    @Transactional
    public CertificateRes addCertificate(Long employeeId, CertificateCreateReq request) {
        Employee employee = getOrThrow(employeeId);
        Certificate certificate = Certificate.builder()
                .employee(employee)
                .name(request.name())
                .issuer(request.issuer())
                .licenseNo(request.licenseNo())
                .acquiredDate(request.acquiredDate())
                .expiryDate(request.expiryDate())
                .build();
        return CertificateRes.from(certificateRepository.save(certificate));
    }

    @Transactional
    public void deleteCertificate(Long certificateId) {
        certificateRepository.deleteById(certificateId);
    }

    @Transactional(readOnly = true)
    public List<FamilyMemberRes> findFamilyMembers(Long employeeId) {
        return familyMemberRepository.findByEmployeeIdOrderByIdAsc(employeeId).stream()
                .map(FamilyMemberRes::from)
                .toList();
    }

    @Transactional
    public FamilyMemberRes addFamilyMember(Long employeeId, FamilyMemberCreateReq request) {
        Employee employee = getOrThrow(employeeId);
        FamilyMember member = FamilyMember.builder()
                .employee(employee)
                .name(request.name())
                .relation(request.relation())
                .birthDate(request.birthDate())
                .dependent(Boolean.TRUE.equals(request.dependent()))
                .cohabiting(Boolean.TRUE.equals(request.cohabiting()))
                .build();
        return FamilyMemberRes.from(familyMemberRepository.save(member));
    }

    @Transactional
    public void deleteFamilyMember(Long familyMemberId) {
        familyMemberRepository.deleteById(familyMemberId);
    }

    @Transactional(readOnly = true)
    public List<EmploymentHistoryRes> findHistories(Long employeeId) {
        return employmentHistoryRepository
                .findByEmployeeIdOrderByEffectiveDateDescIdDesc(employeeId).stream()
                .map(EmploymentHistoryRes::from)
                .toList();
    }

    // MARK: 내부 헬퍼

    // NOTE: 부서와 직위가 함께 바뀌면 전보와 승진을 각각 남긴다. 한 건으로 합치면 전보 사실이 사라진다
    private void recordOrgChange(
            Employee employee, Long beforeDepartmentId, Long beforePositionId, String reason) {
        Long afterDepartmentId = idOf(employee.getDepartment());
        Long afterPositionId = idOf(employee.getJobPosition());

        if (!Objects.equals(beforeDepartmentId, afterDepartmentId)) {
            EmploymentHistory history = newHistory(
                    employee, EmploymentHistory.ChangeType.TRANSFER, LocalDate.now(),
                    reason != null ? reason : "부서 이동");
            history.setBeforeDepartmentId(beforeDepartmentId);
            history.setAfterDepartmentId(afterDepartmentId);
            employmentHistoryRepository.save(history);
        }

        if (!Objects.equals(beforePositionId, afterPositionId)) {
            EmploymentHistory history = newHistory(
                    employee, EmploymentHistory.ChangeType.PROMOTION, LocalDate.now(),
                    reason != null ? reason : "직위 변경");
            history.setBeforePositionId(beforePositionId);
            history.setAfterPositionId(afterPositionId);
            employmentHistoryRepository.save(history);
        }
    }

    private void saveHistory(
            Employee employee, EmploymentHistory.ChangeType type, LocalDate effectiveDate, String reason) {
        EmploymentHistory history = newHistory(employee, type, effectiveDate, reason);
        history.setBeforeDepartmentId(idOf(employee.getDepartment()));
        history.setBeforePositionId(idOf(employee.getJobPosition()));
        employmentHistoryRepository.save(history);
    }

    private EmploymentHistory newHistory(
            Employee employee, EmploymentHistory.ChangeType type, LocalDate effectiveDate, String reason) {
        return EmploymentHistory.builder()
                .employee(employee)
                .changeType(type)
                .effectiveDate(effectiveDate)
                .reason(reason)
                .build();
    }

    private <T> void apply(JsonNullable<T> field, java.util.function.Consumer<T> setter) {
        if (field != null && field.isPresent()) {
            setter.accept(field.get());
        }
    }

    private <T> T unwrap(JsonNullable<T> field) {
        return (field != null && field.isPresent()) ? field.get() : null;
    }

    private Long idOf(Object entity) {
        if (entity instanceof Department department) {
            return department.getId();
        }
        if (entity instanceof JobPosition position) {
            return position.getId();
        }
        return null;
    }

    private Department resolveDepartment(Long departmentId) {
        if (departmentId == null) {
            return null;
        }
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> BusinessException.notFound(ErrorCode.DEPARTMENT_NOT_FOUND, departmentId));
    }

    private JobPosition resolvePosition(Long positionId) {
        if (positionId == null) {
            return null;
        }
        return jobPositionRepository.findById(positionId)
                .orElseThrow(() -> BusinessException.notFound(ErrorCode.POSITION_NOT_FOUND, positionId));
    }

    private Employee resolveManager(Long managerId) {
        if (managerId == null) {
            return null;
        }
        return employeeRepository.findById(managerId)
                .orElseThrow(() -> BusinessException.notFound(ErrorCode.EMPLOYEE_NOT_FOUND, managerId));
    }

    private Employee getOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound(ErrorCode.EMPLOYEE_NOT_FOUND, id));
    }

    private Employee getWithOrgOrThrow(Long id) {
        return employeeRepository.findWithOrgById(id)
                .orElseThrow(() -> BusinessException.notFound(ErrorCode.EMPLOYEE_NOT_FOUND, id));
    }

    private String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
