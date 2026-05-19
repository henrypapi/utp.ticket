package com.pe.limon.api.transactions.authz.business.collaborator.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CollaboratorDetails {
    private Long id;
    private String userId;
    private String ownerUserId;
    private boolean checkAllEvents;
    private LocalDateTime regDatetime;
    private Long regTimestamp;
    private List<Long> eventIds;
}
