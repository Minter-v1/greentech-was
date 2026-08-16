package com.greentech.attendance.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 근무시간 산정 규칙
 *
 * NOTE: 표준 근무 09:00~18:00, 휴게 12:00~13:00, 1일 정규 8시간 기준
 * NOTE: 야간은 22:00~06:00 구간
 * TODO: 교대 근무조 도입 시 사원별 근무제도(work_schedule) 기준으로 대체 필요
 */
public final class WorkTimePolicy {

    public static final LocalTime WORK_START = LocalTime.of(9, 0);
    public static final LocalTime WORK_END = LocalTime.of(18, 0);
    public static final LocalTime BREAK_START = LocalTime.of(12, 0);
    public static final LocalTime BREAK_END = LocalTime.of(13, 0);
    public static final LocalTime NIGHT_START = LocalTime.of(22, 0);
    public static final LocalTime NIGHT_END = LocalTime.of(6, 0);

    public static final int REGULAR_MINUTES_PER_DAY = 480;

    private WorkTimePolicy() {
    }

    /** 휴게시간을 제외한 실근무 분 */
    public static int netWorkedMinutes(LocalDateTime checkIn, LocalDateTime checkOut) {
        long total = Duration.between(checkIn, checkOut).toMinutes();
        if (total <= 0) {
            return 0;
        }
        long breakOverlap = overlapMinutes(
                checkIn, checkOut,
                checkIn.toLocalDate().atTime(BREAK_START),
                checkIn.toLocalDate().atTime(BREAK_END));
        return (int) Math.max(0, total - breakOverlap);
    }

    /** 야간 근무 분 - 당일 22시부터 익일 06시까지 두 구간을 합산 */
    public static int nightMinutes(LocalDateTime checkIn, LocalDateTime checkOut) {
        long minutes = 0;

        LocalDateTime eveningStart = checkIn.toLocalDate().atTime(NIGHT_START);
        LocalDateTime eveningEnd = checkIn.toLocalDate().plusDays(1).atTime(NIGHT_END);
        minutes += overlapMinutes(checkIn, checkOut, eveningStart, eveningEnd);

        LocalDateTime earlyStart = checkIn.toLocalDate().atStartOfDay();
        LocalDateTime earlyEnd = checkIn.toLocalDate().atTime(NIGHT_END);
        minutes += overlapMinutes(checkIn, checkOut, earlyStart, earlyEnd);

        return (int) minutes;
    }

    public static boolean isLate(LocalDateTime checkIn) {
        return checkIn.toLocalTime().isAfter(WORK_START);
    }

    public static boolean isEarlyLeave(LocalDateTime checkOut) {
        return checkOut.toLocalTime().isBefore(WORK_END);
    }

    private static long overlapMinutes(
            LocalDateTime aStart, LocalDateTime aEnd, LocalDateTime bStart, LocalDateTime bEnd) {
        LocalDateTime start = aStart.isAfter(bStart) ? aStart : bStart;
        LocalDateTime end = aEnd.isBefore(bEnd) ? aEnd : bEnd;
        long minutes = Duration.between(start, end).toMinutes();
        return Math.max(0, minutes);
    }
}
