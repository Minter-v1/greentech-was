package com.greentech.leave.service;

import com.greentech.attendance.domain.WorkCalendar;
import com.greentech.attendance.repository.WorkCalendarRepository;
import com.greentech.common.dto.res.PageResult;
import com.greentech.common.enums.ApprovalStatus;
import com.greentech.common.exception.BusinessException;
import com.greentech.common.exception.ErrorCode;
import com.greentech.employee.domain.Employee;
import com.greentech.employee.repository.EmployeeRepository;
import com.greentech.leave.domain.LeaveBalance;
import com.greentech.leave.domain.LeaveRequest;
import com.greentech.leave.domain.LeaveType;
import com.greentech.leave.dto.req.LeaveRejectReq;
import com.greentech.leave.dto.req.LeaveRequestCreateReq;
import com.greentech.leave.dto.res.LeaveBalanceRes;
import com.greentech.leave.dto.res.LeaveRequestRes;
import com.greentech.leave.dto.res.LeaveTypeRes;
import com.greentech.leave.repository.LeaveBalanceRepository;
import com.greentech.leave.repository.LeaveRequestRepository;
import com.greentech.leave.repository.LeaveTypeRepository;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 휴가 신청·결재 및 잔여 관리 */
@Service
@RequiredArgsConstructor
public class LeaveService {

    private static final BigDecimal HALF_DAY = new BigDecimal("0.5");

    // NOTE: 중복 판정 대상 - 반려·취소 건은 기간이 겹쳐도 무시
    private static final List<ApprovalStatus> BLOCKING_STATUSES =
            List.of(ApprovalStatus.REQUESTED, ApprovalStatus.APPROVED);

    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkCalendarRepository workCalendarRepository;

    @Transactional(readOnly = true)
    public List<LeaveTypeRes> findTypes() {
        return leaveTypeRepository.findByActiveTrueOrderByIdAsc().stream()
                .map(LeaveTypeRes::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveBalanceRes> findBalances(Long employeeId, int year) {
        return leaveBalanceRepository.findByEmployeeIdAndYearOrderByIdAsc(employeeId, year).stream()
                .map(LeaveBalanceRes::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResult<LeaveRequestRes> findMyRequests(Long employeeId, Pageable pageable) {
        Page<LeaveRequest> page = leaveRequestRepository.findByEmployeeIdOrderByIdDesc(employeeId, pageable);
        return PageResult.of(page, LeaveRequestRes::from);
    }

    @Transactional(readOnly = true)
    public PageResult<LeaveRequestRes> findRequests(ApprovalStatus status, Pageable pageable) {
        Page<LeaveRequest> page = status == null
                ? leaveRequestRepository.findAllByOrderByIdDesc(pageable)
                : leaveRequestRepository.findByStatusOrderByIdDesc(status, pageable);
        return PageResult.of(page, LeaveRequestRes::from);
    }

    /**
     * 휴가 신청
     *
     * NOTE: 신청 시점에 잔여를 차감하지 않고 승인 시점에 차감
     */
    @Transactional
    public LeaveRequestRes create(Long employeeId, LeaveRequestCreateReq request) {
        if (request.startDate().isAfter(request.endDate())) {
            throw new BusinessException(ErrorCode.INVALID_LEAVE_PERIOD);
        }

        boolean halfDay = Boolean.TRUE.equals(request.halfDay());
        if (halfDay && !request.startDate().equals(request.endDate())) {
            throw new BusinessException(
                    ErrorCode.INVALID_LEAVE_PERIOD, "반차는 시작일과 종료일이 같아야 합니다");
        }

        long overlapping = leaveRequestRepository.countOverlapping(
                employeeId, request.startDate(), request.endDate(), BLOCKING_STATUSES);
        if (overlapping > 0) {
            throw new BusinessException(ErrorCode.OVERLAPPING_LEAVE);
        }

        Employee employee = getEmployeeOrThrow(employeeId);
        LeaveType leaveType = leaveTypeRepository.findById(request.leaveTypeId())
                .orElseThrow(() -> BusinessException.notFound(
                        ErrorCode.LEAVE_TYPE_NOT_FOUND, request.leaveTypeId()));

        BigDecimal days = halfDay
                ? HALF_DAY
                : BigDecimal.valueOf(countWorkdays(request.startDate(), request.endDate()));

        if (days.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_LEAVE_PERIOD, "신청 기간에 근무일이 없습니다");
        }

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(leaveType)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .days(days)
                .halfDay(halfDay)
                .reason(request.reason())
                .status(ApprovalStatus.REQUESTED)
                .build();

        return LeaveRequestRes.from(leaveRequestRepository.save(leaveRequest));
    }

    @Transactional
    public LeaveRequestRes approve(Long requestId, Long approverEmployeeId) {
        LeaveRequest request = getRequestOrThrow(requestId);
        ensureProcessable(request);

        LeaveType leaveType = request.getLeaveType();
        if (leaveType.isDeductAnnual()) {
            deductBalance(request);
        }

        request.approve(getEmployeeOrThrow(approverEmployeeId), LocalDateTime.now());
        return LeaveRequestRes.from(request);
    }

    @Transactional
    public LeaveRequestRes reject(Long requestId, Long approverEmployeeId, LeaveRejectReq body) {
        LeaveRequest request = getRequestOrThrow(requestId);
        ensureProcessable(request);

        request.reject(getEmployeeOrThrow(approverEmployeeId), LocalDateTime.now(), body.rejectReason());
        return LeaveRequestRes.from(request);
    }

    /**
     * 휴가 취소
     *
     * NOTE: 승인 상태에서 취소하면 차감된 잔여를 되돌림
     */
    @Transactional
    public LeaveRequestRes cancel(Long requestId, Long employeeId) {
        LeaveRequest request = getRequestOrThrow(requestId);

        if (!request.getEmployee().getId().equals(employeeId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인이 신청한 휴가만 취소할 수 있습니다");
        }
        if (request.getStatus() == ApprovalStatus.CANCELED) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 취소된 신청입니다");
        }
        if (request.getStatus() == ApprovalStatus.REJECTED) {
            throw new BusinessException(ErrorCode.CONFLICT, "반려된 신청은 취소할 수 없습니다");
        }

        if (request.getStatus() == ApprovalStatus.APPROVED && request.getLeaveType().isDeductAnnual()) {
            restoreBalance(request);
        }

        request.cancel();
        return LeaveRequestRes.from(request);
    }

    // MARK: 내부 헬퍼

    private void ensureProcessable(LeaveRequest request) {
        if (request.getStatus() != ApprovalStatus.REQUESTED) {
            throw new BusinessException(ErrorCode.LEAVE_ALREADY_PROCESSED);
        }
    }

    private void deductBalance(LeaveRequest request) {
        LeaveBalance balance = getBalanceOrThrow(request);
        if (balance.getRemainingDays().compareTo(request.getDays()) < 0) {
            throw new BusinessException(
                    ErrorCode.INSUFFICIENT_LEAVE,
                    "잔여 %s일로 %s일을 승인할 수 없습니다"
                            .formatted(balance.getRemainingDays(), request.getDays()));
        }
        balance.use(request.getDays());
    }

    private void restoreBalance(LeaveRequest request) {
        getBalanceOrThrow(request).restore(request.getDays());
    }

    private LeaveBalance getBalanceOrThrow(LeaveRequest request) {
        int year = request.getStartDate().getYear();
        return leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(
                        request.getEmployee().getId(), request.getLeaveType().getId(), year)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.LEAVE_BALANCE_NOT_FOUND,
                        "%d년 %s 부여 내역이 없습니다".formatted(year, request.getLeaveType().getName())));
    }

    /**
     * 신청 기간의 근무일 수 계산
     *
     * NOTE: 달력 등록분 우선, 미등록 날짜는 요일로 판정
     */
    private long countWorkdays(LocalDate start, LocalDate end) {
        Map<LocalDate, WorkCalendar.DayType> registered =
                workCalendarRepository.findByCalendarDateBetweenOrderByCalendarDateAsc(start, end).stream()
                        .collect(Collectors.toMap(
                                WorkCalendar::getCalendarDate,
                                WorkCalendar::getDayType,
                                (left, right) -> left));

        return start.datesUntil(end.plusDays(1))
                .filter(date -> isWorkday(date, registered))
                .count();
    }

    private boolean isWorkday(LocalDate date, Map<LocalDate, WorkCalendar.DayType> registered) {
        WorkCalendar.DayType dayType = registered.get(date);
        if (dayType != null) {
            return dayType == WorkCalendar.DayType.WORKDAY;
        }
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }

    private LeaveRequest getRequestOrThrow(Long id) {
        return leaveRequestRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound(ErrorCode.LEAVE_REQUEST_NOT_FOUND, id));
    }

    private Employee getEmployeeOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound(ErrorCode.EMPLOYEE_NOT_FOUND, id));
    }
}
