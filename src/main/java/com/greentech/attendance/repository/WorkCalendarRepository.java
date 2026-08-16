package com.greentech.attendance.repository;

import com.greentech.attendance.domain.WorkCalendar;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkCalendarRepository extends JpaRepository<WorkCalendar, Long> {

    Optional<WorkCalendar> findByCalendarDate(LocalDate calendarDate);

    boolean existsByCalendarDate(LocalDate calendarDate);

    List<WorkCalendar> findByCalendarDateBetweenOrderByCalendarDateAsc(LocalDate from, LocalDate to);
}
