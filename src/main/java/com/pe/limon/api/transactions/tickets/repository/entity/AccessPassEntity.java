package com.pe.limon.api.transactions.tickets.repository.entity;

import lombok.Data;

import java.time.LocalDateTime;

import com.pe.limon.api.transactions.authz.repository.entity.UserEntity;

@Data
public class AccessPassEntity {
    private Long id;
    private Long ticketId;
    private TicketEntity ticket;
    private String code;
    private String assignedUserId;
    private UserEntity assignedUser;
    private String assignedBy;
    private LocalDateTime assignedDatetime;
    private Long assignedTimestamp;
    private LocalDateTime usedDatetime;
    private Long usedTimestamp;
    private String status;
    private String admissionStatus;
    private String ticketTypeName;
    private String url;
}