package com.pe.limon.api.transactions.tickets.bussiness;

import com.pe.limon.api.core.utils.conversor.EpochUtils;
import com.pe.limon.api.core.utils.enums.StatusEnum;
import com.pe.limon.api.core.utils.exception.BusinessException;
import com.pe.limon.api.core.utils.exception.InternalServerException;
import com.pe.limon.api.core.utils.exception.NotFoundException;
import com.pe.limon.api.core.utils.generics.PaginationUtil;
import com.pe.limon.api.core.utils.security.SignatureUtil;
import com.pe.limon.api.gateway.admin.access.dto.*;
import com.pe.limon.api.gateway.admin.events.dto.PageResult;
import com.pe.limon.api.transactions.authz.repository.entity.PersonalInfoEntity;
import com.pe.limon.api.transactions.authz.repository.entity.UserEntity;
import com.pe.limon.api.transactions.tickets.bussiness.model.ValidationResult;
import com.pe.limon.api.transactions.tickets.repository.entity.AccessPassEntity;
import com.pe.limon.api.transactions.events.repository.EventRepository;

import java.util.List;
import java.util.stream.Collectors;
import com.pe.limon.api.transactions.tickets.repository.ControlAccessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class ControlAccessService {

    private static final Logger log = LoggerFactory.getLogger(ControlAccessService.class);

    private final ControlAccessRepository controlAccessRepository;
    private final  EventRepository eventRepository;
    private final SignatureUtil signatureUtil;

    public PageResult<AccessPassDTO> getAccessPassesByEventId(Long eventId, AccessPassFiltersDTO filters, int page, int size) {
        int[] validatedParams = PaginationUtil.validatePageAndSize(page, size);
        page = validatedParams[0];
        size = validatedParams[1];

        PageResult<AccessPassEntity> entityResult = controlAccessRepository.getAccessPassesByEventId(eventId, filters, page, size);

        List<AccessPassDTO> dtoList = entityResult.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageResult<>(dtoList, entityResult.getPage(), entityResult.getSize(), entityResult.getTotalElements());
    }

    public ScanAndAdmitResponseDTO scanAndAdmit(String userId, Long eventId, ScanAndAdmitRequestDTO request) {
        log.info("[scanAndAdmit] Starting process: {}", userId);

        // 1) Precondiciones (validaciones duras)
        if (!signatureUtil.verify(request.code(),request.sig())) throw new BusinessException("Fraude detectado en el ticket");

        // Evento válido y QR habilitado
        log.info("[scanAndAdmit] Starting event validation: {}", userId);
        if (!eventRepository.isEventValidAndQrEnabled(eventId)) throw new BusinessException("QR no habilitado para el evento");

        log.info("[scanAndAdmit] Ending event validation: {}", userId);

        // Resolver ap por code
        log.info("[scanAndAdmit] Starting access pass resolution: {}", userId);
        AccessPassEntity accessPass = controlAccessRepository.findByCode(request.code());

        if (accessPass  == null) throw new NotFoundException("Pase de acceso no encontrado para el código proporcionado");

        return admit(accessPass,userId, eventId, true);
    }

    public ScanAndAdmitResponseDTO checkIn(Long accessId, String userId, Long eventId){
        log.info("[checkIn] Starting access pass resolution: {}", userId);

        log.info("[checkIn] Starting event validation: {}", userId);
        if (eventRepository.isEventValidAndQrEnabled(eventId)) throw new BusinessException("No se puede check-in porque se habilito el QR para este evento.");

        AccessPassEntity accessPass;
        try {
            accessPass = controlAccessRepository.findById(accessId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Pase de acceso no encontrado para el código proporcionado");
        }

        log.info("[checkIn] Ending access pass resolution: {}", userId);
        return admit(accessPass,userId, eventId, false);
    }

    private ScanAndAdmitResponseDTO admit(AccessPassEntity accessPass, String userId, Long eventId, boolean qrEnabled){

        var validationResult = validate(accessPass,userId, eventId, qrEnabled);
        long currentTimestamp = EpochUtils.inSeconds();

        return convertToScanAndAdmitResponse(validationResult, accessPass, currentTimestamp);
    }
    private ValidationResult validate(AccessPassEntity accessPass, String userId, Long eventId, boolean qrEnabled){
        if (accessPass==null) throw new NotFoundException("Pase de acceso no encontrado para el código proporcionado");
        long currentTimestamp = EpochUtils.inSeconds();

        log.info("[admit] Starting access pass resolution: {}", userId);
        // Pertenencia del pase al evento
        log.info("[scanAndAdmit] Starting event belonging check: {}", userId);
        if (!eventId.equals(accessPass.getTicket().getTicketType().getEventId())) throw new NotFoundException("Pase de acceso no pertenece a este evento");
        log.info("[admit] Ending admission check: {}", userId);

        if (!accessPass.getAdmissionStatus().equals("P")) {
            log.error("[admit] Access pass not pending: {}", userId);
            return new ValidationResult(true, "Este ticket ya ha sido utilizado", accessPass.getUsedTimestamp());
        }
        log.info("[admit] Ending event belonging check: {}", userId);

        // Estado del pase (ap.status)
        log.info("[admit] Starting status check: {}", userId);
        if (!StatusEnum.ACTIVE.getCode().equals(accessPass.getStatus()))
            return new ValidationResult(true, "El ticket " + accessPass.getTicket().getTicketType().getName() + " ha sido desactivado", null);

        log.info("[scanAndAdmit] Ending status check: {}", userId);

        // Ventana de habilitación del tipo de ticket
        log.info("[admit] Starting time window check: {}", userId);
        ValidationResult window = isTicketTypeWithinValidWindow(accessPass, currentTimestamp);
        if (window.forbidden()) return window;


        log.info("[admit] Ending time window check: {}", userId);

        log.info("[admit] Starting admission update: {}- {} - {}", userId, accessPass.getId(),currentTimestamp);
        int rowsAffected = controlAccessRepository.updateAccessPassForAdmission(accessPass.getId(), currentTimestamp);
        if (rowsAffected == 0)
            return new ValidationResult(true, "Fallo al admitir pase de acceso - puede haber sido procesado por otra solicitud", null);

        log.info("[admit] Ending admission update: {}", userId);
        log.info("[admit] Ending process: {}", userId);
        return new ValidationResult(false, "Acceso permitido, validación exitosa", currentTimestamp);
    }

    private ValidationResult isTicketTypeWithinValidWindow(AccessPassEntity accessPassEntity, long currentTimestamp) {
        var ticketType = accessPassEntity.getTicket().getTicketType();
        if (ticketType == null ) throw new InternalServerException("Tipo de ticket no encontrado");

        if (!(ticketType.getValidFromTimestamp() != null && currentTimestamp >= ticketType.getValidFromTimestamp()))
            return new ValidationResult(true, "Aún no es la hora de ingreso para el ticket: "+ ticketType.getName() + "\n" +
                    "Por favor espere hasta la apertura del acceso", null);

        if (!(ticketType.getValidUntilTimestamp() != null && currentTimestamp <= ticketType.getValidUntilTimestamp()))
            return new ValidationResult(true, "El tiempo de ingreso ha expirado para el ticket: "+ ticketType.getName() +".\n" +
                    "Este acceso ya no está disponible.", null);

        return new ValidationResult(false, "ok", currentTimestamp);
    }


    private AccessPassDTO convertToDTO(AccessPassEntity entity) {
        TicketTypeDTO ticketType = new TicketTypeDTO(
                entity.getTicket().getTicketTypeId(),
                entity.getTicket().getLabelName(),
                null
        );
        log.info("accesspadd: {} ", entity);
        AttendeeDTO attendee = mapAttendeeDTO(entity.getAssignedUser());

        return new AccessPassDTO(
                entity.getId(),
                entity.getTicketId(),
                entity.getAdmissionStatus(),
                entity.getAssignedTimestamp(),
                entity.getUsedTimestamp(),
                ticketType,
                attendee
        );
    }

    private AttendeeDTO mapAttendeeDTO(UserEntity assignedUser) {
        AttendeeDTO attendee = null;
        if (assignedUser != null) {
            attendee = new AttendeeDTO(
                assignedUser.getPersonalInfo().getUserId(),
                assignedUser.getPersonalInfo().getFirstName(),
                assignedUser.getPersonalInfo().getLastName(),
                assignedUser.getPersonalInfo().getEmail(),
                assignedUser.getPersonalInfo().getPhoneNumber(),
                assignedUser.getPersonalInfo().getDocumentType(),
                assignedUser.getPersonalInfo().getDocumentNumber()
            );
        }
        return attendee;
    }

    public AccessPassEntity getById(String userId, Long accessPassId){
       return controlAccessRepository.findById(userId,accessPassId);
    }

    private ScanAndAdmitResponseDTO convertToScanAndAdmitResponse(ValidationResult validationResult, AccessPassEntity entity, Long usedTimestamp) {
        TicketTypeDTO ticketType = new TicketTypeDTO(
                entity.getTicket().getTicketTypeId(),
                entity.getTicketTypeName(),
                entity.getTicket().getTicketType().getDescription()
        );

        AttendeeDTO attendee = mapAttendeeDTO(entity.getAssignedUser());

        return new ScanAndAdmitResponseDTO(
                validationResult,
                entity.getTicketId(),
                entity.getAdmissionStatus(),
                StatusEnum.ACTIVE.getCode(),
                validationResult.timestamp(),
                ticketType,
                attendee
        );
    }
}
