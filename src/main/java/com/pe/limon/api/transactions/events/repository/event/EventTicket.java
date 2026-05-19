package com.pe.limon.api.transactions.events.repository.event;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventTicket {
    private long id;
    private Long ticketTypeId;
    private String token;
    private String ticketFile;
    private String secureCode;
    private String status;
    private LocalDateTime usedAt;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private Long eventId;
    private EventEntity event;
}