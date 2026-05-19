package com.pe.limon.api.gateway.admin.events.dto.create;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalTime;

public record BasicInfoDTO (
        @NotNull
        String eventName,
        @NotNull
        String eventDescription,
        @NotNull
        String typeId,
        @NotNull
        MultipartFile image,
        @NotNull
        LocalDate startDate,
        @NotNull
        LocalTime startTime,
        @NotNull
        Boolean qrEnabled,
        @NotNull
        String paymentMode,
        String paymentComment,
        String redirectAfterPay
){
}
