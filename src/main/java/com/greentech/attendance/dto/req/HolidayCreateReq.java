package com.greentech.attendance.dto.req;

import com.greentech.attendance.domain.WorkCalendar;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "근무 달력 등록 요청")
public record HolidayCreateReq(
        @Schema(description = "날짜", example = "2026-02-17")
        @NotNull(message = "날짜는 필수입니다")
        LocalDate calendarDate,

        @Schema(description = "날짜 구분", example = "HOLIDAY")
        @NotNull(message = "날짜 구분은 필수입니다")
        WorkCalendar.DayType dayType,

        @Schema(description = "공휴일명", example = "설날")
        @Size(max = 50)
        String holidayName) {
}
