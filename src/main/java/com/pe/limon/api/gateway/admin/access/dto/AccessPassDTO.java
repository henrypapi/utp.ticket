package com.pe.limon.api.gateway.admin.access.dto;

public record AccessPassDTO(
    Long accessId,
    Long ticketId,
    String admissionStatus,
    Long assignedTimestamp,
    Long usedTimestamp,
    TicketTypeDTO ticketType,
    AttendeeDTO attendee
) {}