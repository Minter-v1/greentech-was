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
import com.greentech.employee.dto.req.EmployeeUpdateReq;
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
import java.util.List;
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

        // NOTE: 입사도 발령의 한 종류 - 이력을 처음부터 남겨야 이후 변동 추적 가능
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

    /**
     * 사원 정보 수정
     *
     * NOTE: 부서·직위 변경 감지 시 발령 이력 자동 생성
     */
    @Transactional
    public EmployeeDetailRes update(Long id, EmployeeUpdateReq request) {
        Employee employee = getWithOrgOrThrow(id);

        Long beforeDepartmentId = employee.getDepartment() != null ? employee.getDepartment().getId() : null;
        Long beforePositionId = employee.getJobPosition() != null ? employee.getJobPosition().getId() : null;

        employee.setName(request.name());
        employee.setNameEn(request.nameEn());
        employee.setBirthDate(request.birthDate());
        employee.setGender(request.gender());
        employee.setEmail(request.email());
        employee.setDepartment(resolveDepartment(request.departmentId()));
        employee.setJobPosition(resolvePosition(request.jobPositionId()));
        employee.setManager(resolveManager(request.managerId()));
        if (request.employmentType() != null) {
            employee.setEmploymentType(request.employmentType());
        }
        if (request.status() != null) {
            employee.setStatus(request.status());
        }

        recordOrgChange(employee, beforeDepartmentId, beforePositionId);
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

    // MARK: 학력·자격증·가족사항

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

    private void recordOrgChange(Employee employee, Long beforeDepartmentId, Long beforePositionId) {
        Long afterDepartmentId = employee.getDepartment() != null ? employee.getDepartment().getId() : null;
        Long afterPositionId = employee.getJobPosition() != null ? employee.getJobPosition().getId() : null;

        boolean departmentChanged = !java.util.Objects.equals(beforeDepartmentId, afterDepartmentId);
        boolean positionChanged = !java.util.Objects.equals(beforePositionId, afterPositionId);
        if (!departmentChanged && !positionChanged) {
            return;
        }

        EmploymentHistory.ChangeType changeType = positionChanged
                ? EmploymentHistory.ChangeType.PROMOTION
                : EmploymentHistory.ChangeType.TRANSFER;

        employmentHistoryRepository.save(EmploymentHistory.builder()
                .employee(employee)
                .changeType(changeType)
                .effectiveDate(java.time.LocalDate.now())
                .beforeDepartmentId(beforeDepartmentId)
                .afterDepartmentId(afterDepartmentId)
                .beforePositionId(beforePositionId)
                .afterPositionId(afterPositionId)
                .reason("사원 정보 수정에 따른 자동 기록")
                .build());
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
