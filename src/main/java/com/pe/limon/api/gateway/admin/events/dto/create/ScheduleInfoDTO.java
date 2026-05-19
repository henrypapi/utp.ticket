package com.pe.limon.api.gateway.admin.events.dto.create;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleInfoDTO(
    @NotNull
    LocalDate startDate,
    @NotNull
    LocalTime startTime,
    @NotNull
    Boolean qrEnabled
){

}
