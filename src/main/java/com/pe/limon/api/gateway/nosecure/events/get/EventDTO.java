package com.pe.limon.api.gateway.nosecure.events.get;

import com.pe.limon.api.core.utils.generics.GeoPoint;

import java.time.LocalDate;
import java.time.LocalTime;

public record EventDTO (
    Long id,
    String name,
    String description,
    String address,
    GeoPoint location,
    String imgUrl,
    LocalDate startDate,
    LocalTime startTime,
    String timeZone,
    Boolean qrEnabled,
    String terms,
    String paymentMode,
    String paymentComment,
    String redirectAfterPayment
){

}
