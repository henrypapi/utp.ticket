package com.pe.limon.api.transactions.authz.repository.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class CollaboratorEntity {
    private Long id;
    private String userId;
    private ProfileEntity profile;
    private String ownerUserId;
    private boolean checkAllEvents;
    private LocalDateTime registeredDatetime;
    private Long registeredTimestamp;
    private String defaultRole;
    private List<EventCollaboratorEntity> eventCollaborator;
}
