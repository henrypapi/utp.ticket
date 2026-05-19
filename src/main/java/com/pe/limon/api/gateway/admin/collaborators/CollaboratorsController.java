package com.pe.limon.api.gateway.admin.collaborators;

import com.pe.limon.api.core.utils.generics.MessageDTO;
import com.pe.limon.api.gateway.admin.collaborators.dto.CollabDetail;
import com.pe.limon.api.gateway.admin.events.dto.PageResult;

import com.pe.limon.api.transactions.authz.business.collaborator.AssignCollaborator;
import com.pe.limon.api.gateway.admin.collaborators.dto.CreateCollabDTO;
import com.pe.limon.api.transactions.authz.business.collaborator.GetCollaborator;
import com.pe.limon.api.transactions.authz.business.collaborator.RemoveCollaborator;
import com.pe.limon.api.transactions.authz.repository.entity.CollaboratorRow;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin/collaborators")
@RequiredArgsConstructor
public class CollaboratorsController {
    private final AssignCollaborator assignCollaborator;
    private final GetCollaborator getCollaborator;
    private final RemoveCollaborator removeCollaborator;

    @GetMapping
    public ResponseEntity<PageResult<CollaboratorRow>> findAll(
            @RequestAttribute(name = "userId") String ownerUserId,
            @RequestParam(required = false) String username,
            @RequestParam int size,
            @RequestParam int page
    ) {
        return ResponseEntity.ok(getCollaborator.getMyCollaborators(username,size,page,ownerUserId));
    }


    @GetMapping("/{collaboratorId}")
    public ResponseEntity<CollabDetail> getById(
            @RequestAttribute String userId,
            @PathVariable Long collaboratorId
    ) {
        return ResponseEntity.ok(getCollaborator.getCollaborator(collaboratorId,userId));
    }

    @PostMapping
    public ResponseEntity<MessageDTO> createCollaborator(
            @RequestAttribute String userId,
            @Valid @RequestBody CreateCollabDTO request
    ) {
        assignCollaborator.execute(request, userId);
        return ResponseEntity.ok(new MessageDTO("ok"));
    }

    @PutMapping("/{collaboratorId}")
    public ResponseEntity<MessageDTO> putCollaborator(
            @RequestAttribute String userId,
            @Valid @RequestBody CollabDetail request,
            @PathVariable Long collaboratorId
    ) {
        assignCollaborator.setDetail(request, userId,collaboratorId);
        return ResponseEntity.ok(new MessageDTO("ok"));
    }


    @DeleteMapping("/{collaboratorId}")
    public ResponseEntity<MessageDTO> deleteCollaborator(
            @RequestAttribute String userId,
            @PathVariable Long collaboratorId
    ) {
        removeCollaborator.execute(collaboratorId,userId);
        return ResponseEntity.ok(new MessageDTO("ok"));
    }
}
