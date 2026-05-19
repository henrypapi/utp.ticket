package com.pe.limon.api.core.utils.conversor;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class TimeUtil {
    private final static String ZONE_ID = "America/Lima";
    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public static String formatInZone(LocalDateTime dateTime, String timeZone) {
        return dateTime
                .atZone(ZoneId.of(timeZone))
                .format(ISO_FMT);
    }

    public static LocalDateTime getLocalDateTime() {
        ZonedDateTime peruTime = ZonedDateTime.now(ZoneId.of(ZONE_ID));

        return peruTime.toLocalDateTime();
    }

    public static LocalTime getLocalTime() {
        ZonedDateTime peruTime = ZonedDateTime.now(ZoneId.of(ZONE_ID));

        return peruTime.toLocalTime();
    }

    public static LocalDate getLocalDate() {
        ZonedDateTime peruTime = ZonedDateTime.now(ZoneId.of(ZONE_ID));

        return peruTime.toLocalDate();
    }
}