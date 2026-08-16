package com.greentech.attendance.domain;

import com.greentech.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * 근무 달력 - 날짜만으로 판별 불가한 공휴일 등만 등록
 *
 * NOTE: 토·일요일은 행 없이 DayType.WEEKEND 로 계산
 */
@Entity
@Table(name = "work_calendar")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkCalendar extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "calendar_date", nullable = false, unique = true)
    private LocalDate calendarDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_type", nullable = false, length = 20)
    private DayType dayType;

    @Column(name = "holiday_name", length = 50)
    private String holidayName;

    public enum DayType {
        WORKDAY, WEEKEND, HOLIDAY
    }
}
