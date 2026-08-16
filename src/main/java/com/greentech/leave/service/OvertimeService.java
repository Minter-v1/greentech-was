package com.greentech.leave.service;

import com.greentech.common.dto.res.PageResult;
import com.greentech.common.enums.ApprovalStatus;
import com.greentech.common.exception.BusinessException;
import com.greentech.common.exception.ErrorCode;
import com.greentech.employee.domain.Employee;
import com.greentech.employee.repository.EmployeeRepository;
import com.greentech.leave.domain.OvertimeRequest;
import com.greentech.leave.dto.req.OvertimeCreateReq;
import com.greentech.leave.dto.res.OvertimeRequestRes;
import com.greentech.leave.repository.OvertimeRequestRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 연장근무 신청·결재 */
@Service
@RequiredArgsConstructor
public class OvertimeService {

    private final OvertimeRequestRepository overtimeRequestRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public OvertimeRequestRes create(Long employeeId, OvertimeCreateReq request) {
        if (!request.endAt().isAfter(request.startAt())) {
            throw new BusinessException(ErrorCode.INVALID_OVERTIME_PERIOD);
        }

        Employee employee = getEmployeeOrThrow(employeeId);
        int minutes = (int) Duration.between(request.startAt(), request.endAt()).toMinutes();

        OvertimeRequest overtime = OvertimeRequest.builder()
                .employee(employee)
                .workDate(request.startAt().toLocalDate())
                .startAt(request.startAt())
                .endAt(request.endAt())
                .minutes(minutes)
                .overtimeType(request.overtimeType())
                .reason(request.reason())
                .status(ApprovalStatus.REQUESTED)
                .build();

        return OvertimeRequestRes.from(overtimeRequestRepository.save(overtime));
    }

    @Transactional(readOnly = true)
    public PageResult<OvertimeRequestRes> findMyRequests(Long employeeId, Pageable pageable) {
        Page<OvertimeRequest> page =
                overtimeRequestRepository.findByEmployeeIdOrderByIdDesc(employeeId, pageable);
        return PageResult.of(page, OvertimeRequestRes::from);
    }

    @Transactional(readOnly = true)
    public PageResult<OvertimeRequestRes> findByStatus(ApprovalStatus status, Pageable pageable) {
        Page<OvertimeRequest> page =
                overtimeRequestRepository.findByStatusOrderByIdDesc(status, pageable);
        return PageResult.of(page, OvertimeRequestRes::from);
    }

    @Transactional
    public OvertimeRequestRes approve(Long requestId, Long approverEmployeeId) {
        OvertimeRequest request = getOrThrow(requestId);
        ensureProcessable(request);
        request.approve(getEmployeeOrThrow(approverEmployeeId), LocalDateTime.now());
        return OvertimeRequestRes.from(request);
    }

    @Transactional
    public OvertimeRequestRes reject(Long requestId, Long approverEmployeeId) {
        OvertimeRequest request = getOrThrow(requestId);
        ensureProcessable(request);
        request.reject(getEmployeeOrThrow(approverEmployeeId), LocalDateTime.now());
        return OvertimeRequestRes.from(request);
    }

    private void ensureProcessable(OvertimeRequest request) {
        if (request.getStatus() != ApprovalStatus.REQUESTED) {
            throw new BusinessException(ErrorCode.OVERTIME_ALREADY_PROCESSED);
        }
    }

    private OvertimeRequest getOrThrow(Long id) {
        return overtimeRequestRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound(ErrorCode.OVERTIME_NOT_FOUND, id));
    }

    private Employee getEmployeeOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound(ErrorCode.EMPLOYEE_NOT_FOUND, id));
    }
}
