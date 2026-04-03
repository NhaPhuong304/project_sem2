package com.aptech.projectmgmt.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    public static final DateTimeFormatter DATE_FORMAT     = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static String format(LocalDate date) {
        return date == null ? "" : date.format(DATE_FORMAT);
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATETIME_FORMAT);
    }

    public static LocalDate parseDate(String text) {
        return (text == null || text.isBlank()) ? null : LocalDate.parse(text, DATE_FORMAT);
    }

    public static LocalDateTime parseDateTime(String text) {
        return (text == null || text.isBlank()) ? null : LocalDateTime.parse(text, DATETIME_FORMAT);
    }
}
