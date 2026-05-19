package com.pe.limon.api.gateway.admin.access.dto;

import com.pe.limon.api.transactions.tickets.bussiness.model.ValidationResult;

public record ScanAndAdmitResponseDTO(
    ValidationResult validationResult,
    Long ticketId,
    String accessStatus,
    String admissionStatus,
    Long usedTimestamp,
    TicketTypeDTO ticketType,
    AttendeeDTO attendee
) {}