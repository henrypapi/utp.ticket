package com.pe.limon.api.transactions.authz.business.collaborator;

import com.pe.limon.api.core.utils.exception.NotFoundException;
import com.pe.limon.api.gateway.admin.collaborators.dto.CollabDetail;
import com.pe.limon.api.gateway.admin.collaborators.dto.EventRole;
import com.pe.limon.api.transactions.authz.repository.entity.CollaboratorRow;
import com.pe.limon.api.transactions.authz.repository.entity.ProfileEntity;
import com.pe.limon.api.gateway.admin.events.dto.PageResult;
import com.pe.limon.api.transactions.authz.repository.CollaboratorRepository;
import com.pe.limon.api.transactions.authz.repository.entity.CollaboratorEntity;
import com.pe.limon.api.transactions.authz.repository.entity.ScopeRoleEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class GetCollaborator {

    private final CollaboratorRepository collaboratorRepository;

    public PageResult<CollaboratorRow> getMyCollaborators(String username, Integer size, Integer page, String ownerUserId) {
        return collaboratorRepository.findByFilters(ownerUserId, username, page, size);
    }

    public CollabDetail getCollaborator(Long collaboratorId, String ownerUserId){
        CollaboratorEntity entity = new CollaboratorEntity();
        entity.setId(collaboratorId);
        entity.setOwnerUserId(ownerUserId);

        var roles = collaboratorRepository.findById(entity);

        return new CollabDetail("",roles.get(0).getScopeType(),
                roles.stream().map(ScopeRoleEntity::getEventIdKey).distinct().toList(),
                roles.stream().map(ScopeRoleEntity::getRoleId).distinct().toList());
    }

}
