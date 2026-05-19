package com.pe.limon.api.transactions.authz.repository.entity;

import lombok.Data;

import java.util.List;

@Data
public class EventCollaboratorEntity {
    private Long collaboratorId;
    private CollaboratorEntity collaborator;
    private Long eventId;
    private List<String> roles;
    private String rolesJson;
    private String username;
    private String userId;
}
