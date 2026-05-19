package com.pe.limon.api.gateway.admin.events.dto.get;

import com.pe.limon.api.core.utils.generics.GeoPoint;

import java.time.LocalDate;

import java.time.LocalTime;

public record EventDTO (
    Long id,
    String name,
    String description,
    String address,
    GeoPoint location,
    Float realPrice,
    LocalDate startDate,
    String imgUrl,
    LocalTime startTime,
    Boolean qrEnabled,
    String terms
){

    public EventDTO(String name) {
        this(null, name, "", "", null, null, null, "", null, null,null);
    }
}
