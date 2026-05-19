package com.pe.limon.api.transactions.authz.business.collaborator;

import com.pe.limon.api.core.utils.exception.BusinessException;
import com.pe.limon.api.core.utils.exception.NotFoundException;
import com.pe.limon.api.transactions.authz.repository.CollaboratorRepository;
import com.pe.limon.api.transactions.authz.repository.entity.CollaboratorScopeEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RemoveCollaborator {

    private final CollaboratorRepository collaboratorRepository;

    @Transactional
    public void execute(Long collaboratorId, String ownerUserId){
        log.info("Executing AssignCollaborator");
        var scopes = collaboratorRepository.findScopeIdsByCollabIdAndOwnerUserId(collaboratorId,ownerUserId);
        if(scopes.isEmpty()) return;
        var scopeIds = scopes.stream().map(CollaboratorScopeEntity::getId).toList();
        int del = collaboratorRepository.deleteScopeRoleByIdAndRoles(scopeIds,List.of(1L,2L,3L,4L));

        if (del == 0) throw new BusinessException("Error al eliminar el colaborador");
        del = collaboratorRepository.deleteScopeByIds(scopeIds);

        if (del == 0) throw new BusinessException("Error al eliminar el colaborador");
        del = collaboratorRepository.deleteCollaboratorByIdAndOwnerUserId(collaboratorId, ownerUserId);

        if (del == 0) throw new BusinessException("Error al eliminar el colaborador");
    }
}
