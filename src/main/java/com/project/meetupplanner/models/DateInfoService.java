package com.project.meetupplanner.models;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import org.springframework.stereotype.Service;

@Service
public class DateInfoService {

    public int getDaysInMonth(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        return yearMonth.lengthOfMonth();
    }

    public DayOfWeek getFirstDayOfMonth(int year, int month) {
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        return firstDayOfMonth.getDayOfWeek();
    }

    public DayOfWeek getLastDayOfMonth(int year, int month) {
        int daysInMonth = getDaysInMonth(year, month);
        LocalDate lastDayOfMonth = LocalDate.of(year, month, daysInMonth);
        return lastDayOfMonth.getDayOfWeek();
    }
}
