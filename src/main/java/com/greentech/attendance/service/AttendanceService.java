package com.greentech.attendance.service;

import com.greentech.attendance.domain.Attendance;
import com.greentech.attendance.domain.WorkCalendar;
import com.greentech.attendance.dto.req.HolidayCreateReq;
import com.greentech.attendance.dto.res.AttendanceMonthlyRes;
import com.greentech.attendance.dto.res.AttendanceRes;
import com.greentech.attendance.dto.res.WorkCalendarRes;
import com.greentech.attendance.repository.AttendanceRepository;
import com.greentech.attendance.repository.WorkCalendarRepository;
import com.greentech.common.exception.BusinessException;
import com.greentech.common.exception.ErrorCode;
import com.greentech.employee.domain.Employee;
import com.greentech.employee.repository.EmployeeRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 출퇴근 기록 및 근무 달력 관리 */
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final WorkCalendarRepository workCalendarRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public AttendanceRes checkIn(Long employeeId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today).ifPresent(existing -> {
            if (existing.getCheckInAt() != null) {
                throw new BusinessException(ErrorCode.ALREADY_CHECKED_IN);
            }
        });

        Employee employee = getEmployeeOrThrow(employeeId);
        Attendance attendance = attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today)
                .orElseGet(() -> Attendance.builder()
                        .employee(employee)
                        .workDate(today)
                        .status(Attendance.Status.NORMAL)
                        .build());

        attendance.setCheckInAt(now);
        attendance.setStatus(resolveCheckInStatus(today, now));

        return AttendanceRes.from(attendanceRepository.save(attendance));
    }

    @Transactional
    public AttendanceRes checkOut(Long employeeId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        Attendance attendance = attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_CHECKED_IN));

        if (attendance.getCheckInAt() == null) {
            throw new BusinessException(ErrorCode.NOT_CHECKED_IN);
        }
        if (attendance.getCheckOutAt() != null) {
            throw new BusinessException(ErrorCode.ALREADY_CHECKED_OUT);
        }

        attendance.setCheckOutAt(now);
        applyWorkedMinutes(attendance);

        if (attendance.getStatus() == Attendance.Status.NORMAL && WorkTimePolicy.isEarlyLeave(now)) {
            attendance.setStatus(Attendance.Status.EARLY_LEAVE);
        }

        return AttendanceRes.from(attendance);
    }

    @Transactional(readOnly = true)
    public AttendanceMonthlyRes findMonthly(Long employeeId, YearMonth yearMonth) {
        LocalDate from = yearMonth.atDay(1);
        LocalDate to = yearMonth.atEndOfMonth();

        List<Attendance> records =
                attendanceRepository.findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(employeeId, from, to);

        int workedDays = 0;
        int lateDays = 0;
        int absentDays = 0;
        int totalWork = 0;
        int totalOvertime = 0;
        int totalNight = 0;

        for (Attendance record : records) {
            switch (record.getStatus()) {
                case NORMAL, EARLY_LEAVE -> workedDays++;
                case LATE -> {
                    workedDays++;
                    lateDays++;
                }
                case ABSENT -> absentDays++;
                default -> {
                }
            }
            totalWork += record.getWorkMinutes();
            totalOvertime += record.getOvertimeMinutes();
            totalNight += record.getNightMinutes();
        }

        return new AttendanceMonthlyRes(
                employeeId,
                yearMonth.toString(),
                workedDays,
                lateDays,
                absentDays,
                totalWork,
                totalOvertime,
                totalNight,
                records.stream().map(AttendanceRes::from).toList());
    }

    // MARK: 근무 달력

    @Transactional(readOnly = true)
    public List<WorkCalendarRes> findCalendar(LocalDate from, LocalDate to) {
        return workCalendarRepository.findByCalendarDateBetweenOrderByCalendarDateAsc(from, to).stream()
                .map(WorkCalendarRes::from)
                .toList();
    }

    @Transactional
    public WorkCalendarRes registerCalendarDay(HolidayCreateReq request) {
        if (workCalendarRepository.existsByCalendarDate(request.calendarDate())) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 등록된 날짜입니다: " + request.calendarDate());
        }

        WorkCalendar calendar = WorkCalendar.builder()
                .calendarDate(request.calendarDate())
                .dayType(request.dayType())
                .holidayName(request.holidayName())
                .build();

        return WorkCalendarRes.from(workCalendarRepository.save(calendar));
    }

    /**
     * 근무일 판정
     *
     * NOTE: 달력에 행이 있으면 그 값 우선, 없으면 요일로 판정
     */
    @Transactional(readOnly = true)
    public boolean isWorkday(LocalDate date) {
        return workCalendarRepository.findByCalendarDate(date)
                .map(calendar -> calendar.getDayType() == WorkCalendar.DayType.WORKDAY)
                .orElseGet(() -> !isWeekend(date));
    }

    // MARK: 내부 헬퍼

    private Attendance.Status resolveCheckInStatus(LocalDate date, LocalDateTime checkInAt) {
        boolean holiday = workCalendarRepository.findByCalendarDate(date)
                .map(calendar -> calendar.getDayType() == WorkCalendar.DayType.HOLIDAY)
                .orElseGet(() -> isWeekend(date));

        if (holiday) {
            return Attendance.Status.HOLIDAY;
        }
        return WorkTimePolicy.isLate(checkInAt) ? Attendance.Status.LATE : Attendance.Status.NORMAL;
    }

    private void applyWorkedMinutes(Attendance attendance) {
        int net = WorkTimePolicy.netWorkedMinutes(attendance.getCheckInAt(), attendance.getCheckOutAt());
        int regular = Math.min(net, WorkTimePolicy.REGULAR_MINUTES_PER_DAY);
        int overtime = Math.max(0, net - WorkTimePolicy.REGULAR_MINUTES_PER_DAY);

        attendance.setWorkMinutes(regular);
        attendance.setOvertimeMinutes(overtime);
        attendance.setNightMinutes(
                WorkTimePolicy.nightMinutes(attendance.getCheckInAt(), attendance.getCheckOutAt()));
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    private Employee getEmployeeOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound(ErrorCode.EMPLOYEE_NOT_FOUND, id));
    }
}
