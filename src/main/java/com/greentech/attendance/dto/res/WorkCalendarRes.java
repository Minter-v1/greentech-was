package com.greentech.attendance.dto.res;

import com.greentech.attendance.domain.WorkCalendar;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "근무 달력 항목")
public record WorkCalendarRes(
        @Schema(description = "달력 ID", example = "1")
        Long id,

        @Schema(description = "날짜", example = "2026-08-15")
        LocalDate calendarDate,

        @Schema(description = "날짜 구분", example = "HOLIDAY")
        WorkCalendar.DayType dayType,

        @Schema(description = "공휴일명", example = "광복절")
        String holidayName) {

    public static WorkCalendarRes from(WorkCalendar entity) {
        return new WorkCalendarRes(
                entity.getId(),
                entity.getCalendarDate(),
                entity.getDayType(),
                entity.getHolidayName());
    }
}
