package com.pe.limon.api.transactions.authz.business.collaborator;

import com.pe.limon.api.core.utils.conversor.EpochUtils;
import com.pe.limon.api.core.utils.exception.BusinessException;
import com.pe.limon.api.gateway.admin.collaborators.dto.CollabDetail;
import com.pe.limon.api.gateway.admin.collaborators.dto.CreateCollabDTO;
import com.pe.limon.api.transactions.authz.repository.CollaboratorRepository;
import com.pe.limon.api.transactions.authz.repository.UserRepository;
import com.pe.limon.api.transactions.authz.repository.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignCollaborator {
    private final CollaboratorRepository collaboratorRepository;
    private final UserRepository userRepository;

    public CollaboratorEntity execute(CreateCollabDTO in, String ownerUserId) {
        log.info("Executing AssignCollaborator");

        var optUser = userRepository.findByEmailOrUsername(in.user(), in.user());
        if (optUser.isEmpty()) throw new BusinessException("El usuario no existe.");

        try {
            var collaboratorEntity = new CollaboratorEntity();
            collaboratorEntity.setRegisteredTimestamp(EpochUtils.inSeconds());
            collaboratorEntity.setRegisteredDatetime(LocalDateTime.now());
            collaboratorEntity.setOwnerUserId(ownerUserId);
            collaboratorEntity.setUserId(optUser.get().getId());
            var id = collaboratorRepository.insert(collaboratorEntity);
            collaboratorEntity.setId(id);
            return collaboratorEntity;
        }catch (DuplicateKeyException e){
            throw new BusinessException("El usuario ya se encuentra registrado.");
        }
    }


    @Transactional
    public void setDetail(CollabDetail in, String ownerUserId, Long collaboratorId) {
        log.info("Executing setDetail {} ", in);

        var scopes = collaboratorRepository.findScopeIdsByCollabIdAndOwnerUserId(collaboratorId,ownerUserId);
        if(!scopes.isEmpty()) {

            var scopeIds = scopes.stream().map(CollaboratorScopeEntity::getId).toList();
            log.info("Collaborator {} scopes {}", collaboratorId, scopeIds);

            collaboratorRepository.deleteScopeRoleByIdAndRoles(scopeIds, List.of(1L,2L,3L,4L));
            collaboratorRepository.deleteScopeByIds(scopeIds);
        }

        if (in.scopeType().equals("EVENT")){
            for (var eventId:in.eventIds()){
                newScope(in, collaboratorId, eventId);
            }
        }

        if(in.scopeType().equals("GLOBAL")){
            newScope(in,collaboratorId,0L);
        }


    }

    private void newScope(CollabDetail in, Long collaboratorId, Long eventId) {
        ScopeEntity scopeEntity = new ScopeEntity();
        scopeEntity.setCollaboratorId(collaboratorId);
        scopeEntity.setScopeType(in.scopeType());
        scopeEntity.setEventId(eventId);
        scopeEntity.setRegisteredDatetime(LocalDateTime.now());
        scopeEntity.setRegisteredTimestamp(EpochUtils.inSeconds());
        log.info("Executing newScope {} ", scopeEntity);

        Long scopeId = collaboratorRepository.insertScope(scopeEntity);
        if (scopeId == null || scopeId == 0) throw new BusinessException("Error en el proceso");
        List<ScopeRoleEntity> scopeRoleEntities = new ArrayList<>();

        for (var roleId : in.roleIds()) {
            ScopeRoleEntity scopeRoleEntity = new ScopeRoleEntity();
            scopeRoleEntity.setScopeId(scopeId);
            scopeRoleEntity.setRoleId(roleId);
            scopeRoleEntity.setRegisteredDatetime(LocalDateTime.now());
            scopeRoleEntity.setRegisteredTimestamp(EpochUtils.inSeconds());
            scopeRoleEntities.add(scopeRoleEntity);
        }
        collaboratorRepository.batchScopeRolesInsert(scopeRoleEntities);
    }
}
