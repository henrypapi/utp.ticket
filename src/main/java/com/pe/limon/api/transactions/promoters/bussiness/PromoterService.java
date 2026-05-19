package com.pe.limon.api.transactions.promoters.bussiness;

import com.pe.limon.api.core.utils.exception.BusinessException;
import com.pe.limon.api.gateway.admin.collaborators.dto.CreateCollabDTO;
import com.pe.limon.api.gateway.admin.events.dto.PageResult;
import com.pe.limon.api.gateway.admin.promoter.dto.CreatePromoterDTO;
import com.pe.limon.api.gateway.admin.promoter.dto.UpdatePromoterDTO;
import com.pe.limon.api.transactions.authz.business.collaborator.AssignCollaborator;
import com.pe.limon.api.transactions.authz.business.collaborator.model.AssignCollabIn;
import com.pe.limon.api.transactions.authz.business.permissions.EffectivePerm;
import com.pe.limon.api.transactions.authz.repository.CollaboratorRepository;
import com.pe.limon.api.transactions.authz.repository.UserRepository;
import com.pe.limon.api.transactions.events.repository.EventRepository;
import com.pe.limon.api.transactions.promoters.bussiness.entity.PromoterEntity;
import com.pe.limon.api.transactions.promoters.repository.PromoterRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromoterService {

    private final PromoterRepository repository;
    private final UserRepository userRepository;
    private final CollaboratorRepository collaboratorRepository;
    private final EventRepository eventRepository;
    private final AssignCollaborator assignCollaborator;

    public PageResult<PromoterEntity> findByEventId(Long eventId, String username, String code, int page, int size) {
        return repository.findByEventIdWithFilters(eventId, username, code, page, size);
    }

    public List<EffectivePerm> getEventByUserId(String userId) {
        return repository.findEventByUserId(userId);
    }

    public PromoterEntity findByEventIdAndCode(String userId,Long eventId, String code) {

        return repository.findByEventIdAndCode(eventId, code,userId);
    }

    public void save(Long eventId, CreatePromoterDTO dto, String registerBy) {
        var event = eventRepository.findById(eventId);

        if (event == null)  throw new BusinessException("EVENTO NO DISPONIBLE");

        switch (event.getStatus()){
            case "A":
                break;
            case "P":
                throw new BusinessException("El evento se encuentra deshabilitado. Para poder registrar un promotor, vuelve a activarlo en el MODULO EVENTOS.");
            default:
                throw new BusinessException("EVENTO NO DISPONIBLE");
        }

        var optUser = userRepository.findByEmailOrUsername(dto.promoterUser(),dto.promoterUser());
        if (optUser.isEmpty()) throw new BusinessException("El email o username del usuario no existe.");

        PromoterEntity entity = new PromoterEntity();
        entity.setEventId(eventId);
        entity.setCode(dto.code());
        entity.setPromoterUserId(optUser.get().getId());
        entity.setMaxUses(dto.maxUses());
        entity.setActive(dto.isActive());
        entity.setRegisteredDatetime(LocalDateTime.now());
        entity.setRegisteredTimestamp(System.currentTimeMillis());
        entity.setRegisteredBy(registerBy);

        try{
            repository.save(entity);
        }catch (DuplicateKeyException e){
            throw new BusinessException("El código ingresado ya se encuentra registrado.");
        }
    }

    public void update(Long eventId, Long promoterId, UpdatePromoterDTO dto, String registerBy) {
        PromoterEntity entity = new PromoterEntity();
        entity.setEventId(eventId);
        entity.setId(promoterId);
        entity.setMaxUses(dto.maxUses());
        entity.setActive(dto.isActive());
        repository.update(entity);
    }
}
