package com.pe.limon.api.transactions.authz.business.collaborator.model;

import com.pe.limon.api.gateway.admin.collaborators.dto.EventRolesDTO;
import lombok.Data;

import java.util.List;

@Data
public class AssignCollabIn {
    String user;
    List<Long> eventIds;
    String roleId;
    String ownerUserId;
    boolean checkAllEvents;
    boolean newCollaborator;
    Long collaboratorId;
    Long eventCollabId;
}
