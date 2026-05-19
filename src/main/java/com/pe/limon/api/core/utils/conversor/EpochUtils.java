package com.pe.limon.api.core.utils.conversor;

import java.time.*;

public class EpochUtils {

    public static long inSeconds(){
        return System.currentTimeMillis() / 1000;
    }

    public static LocalDateTime epochToLocalDateTime(long epoch){
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(epoch),
                ZoneOffset.UTC   // UTC-0
        );
    }
    public static long localDateTimeToEpochSeconds(LocalDateTime dateTime, ZoneId zoneId) {
        return dateTime.atZone(zoneId).toEpochSecond();
    }


    public static LocalDateTime epochToLocalDateTimeWithZone(long epoch, ZoneId zoneOffset){
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(epoch),
                zoneOffset   // UTC-0
        );
    }
}
