package com.pe.limon.api.transactions.tickets.bussiness;

import com.pe.limon.api.core.utils.conversor.EpochUtils;
import com.pe.limon.api.core.utils.enums.StatusEnum;
import com.pe.limon.api.gateway.nosecure.events.get.TicketDTO;
import com.pe.limon.api.gateway.admin.tickets.dto.types.CreateDTO;
import com.pe.limon.api.gateway.admin.tickets.dto.types.UpdateDTO;
import com.pe.limon.api.core.utils.exception.BusinessException;
import com.pe.limon.api.transactions.events.repository.event.EventEntity;
import com.pe.limon.api.transactions.tickets.repository.entity.TicketType;
import com.pe.limon.api.transactions.events.repository.EventRepository;
import com.pe.limon.api.transactions.tickets.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class TypeManager {

    private final TicketTypeRepository ticketTypeRepository;
    private final EventRepository eventRepository;

    @Transactional
    public void addTicketToEvent(CreateDTO dto, String userId){
        log.info("[addTicketToEvent] Starting - {}", userId);
        log.info("[addTicketToEvent] CreateDTO - {}", dto);

        TicketType ticketType = validEventQrConfig(dto.eventId(),
                dto.validFromTimestamp(), dto.qrDurationHours(),
                dto.enableStartTimestamp(),
                dto.enableEndTimestamp());

        ticketType.setName(dto.name());
        ticketType.setDescription(dto.description());
        ticketType.setStock(dto.stock());
        ticketType.setPurchaseQuantityLimit(dto.purchaseQuantityLimit());
        ticketType.setSeatsPerGroup(dto.seatsPerGroup());
        ticketType.setRegisteredTimestamp(System.currentTimeMillis());
        ticketType.setPrice(BigDecimal.valueOf(dto.free() ? 0: dto.realPrice()));

        ticketType.setEnableStartDatetime(EpochUtils.epochToLocalDateTime(dto.enableStartTimestamp()));
        ticketType.setEnableStartTimestamp(+dto.enableStartTimestamp());

        ticketType.setEnableEndDatetime(EpochUtils.epochToLocalDateTime(dto.enableEndTimestamp()));
        ticketType.setEnableEndTimestamp(dto.enableEndTimestamp());

        ticketType.setActive(dto.active());
        ticketType.setEnableVoucher(dto.enableVoucher());
        ticketType.setRegisteredTimestamp(System.currentTimeMillis());
        ticketType.setRegisteredDatetime(LocalDateTime.now());
        ticketType.setRegisteredBy(userId);
        ticketType.setSold(0);
        ticketType.setReserved(0);
        log.info("[addTicketToEvent] save - {}", ticketType);
        ticketTypeRepository.save(ticketType);
        log.info("[addTicketToEvent] success process - {}", userId);
    }

    @Transactional
    public void updateTicketType(UpdateDTO dto, Long ticketTypeId,String userId){
        log.info("[updateTicketType] Starting - {}", userId);

        TicketType gotTicket = ticketTypeRepository.findByIdAndOwnerUserId(ticketTypeId,userId);
        if(gotTicket == null) throw new BusinessException("Este evento no existe.");

        TicketType ticketType = validEventQrConfig(gotTicket.getEventId(), dto.validFromTimestamp(),
                dto.qrDurationHours(),dto.enableStartTimestamp(),
                dto.enableEndTimestamp());

        ticketType.setId(ticketTypeId);
        ticketType.setName(dto.name());
        ticketType.setDescription(dto.description());
        ticketType.setStock(dto.stock());
        ticketType.setRegisteredTimestamp(System.currentTimeMillis());
        ticketType.setPrice(BigDecimal.valueOf(dto.free() ? 0: dto.realPrice()));
        ticketType.setPurchaseQuantityLimit(dto.purchaseQuantityLimit());
        ticketType.setSeatsPerGroup(dto.seatsPerGroup());

        ticketType.setEnableStartTimestamp(dto.enableStartTimestamp());
        ticketType.setEnableEndTimestamp(dto.enableEndTimestamp());
        ticketType.setEnableEndDatetime(EpochUtils.epochToLocalDateTime(dto.enableEndTimestamp()));
        ticketType.setEnableStartDatetime(EpochUtils.epochToLocalDateTime(dto.enableStartTimestamp()));

        ticketType.setActive(dto.active());
        ticketType.setEnableVoucher(dto.enableVoucher());
        log.info("[updateTicketType] ticketType - {}", ticketType);
        ticketTypeRepository.update(ticketType);
        log.info("[updateTicketType] success process - {}", userId);
    }

    private TicketType validEventQrConfig(long eventId,
                                          Long timestamp,
                                          Integer integer,
                                          Long enableStartTimestamp,
                                          Long enableEndTimestamp) {

        EventEntity eventEntity = eventRepository.findById(eventId);
        if(eventEntity == null) throw new BusinessException("Este evento no existe.");

        switch (eventEntity.getStatus()){
            case "A":
                break;
            case "P":
                throw new BusinessException("El evento se encuentra deshabilitado. Para poder registrar un ticket, vuelve a activar el evento en el MODULO EVENTOS.");
            default:
                throw new BusinessException("EVENTO NO DISPONIBLE");
        }

        if (enableStartTimestamp>enableEndTimestamp)
            throw new BusinessException("La fecha inicio no puede ser mayor a la fin.");

        LocalDateTime eventStartDatetime = eventEntity.getStartDatetime();
        long epochSeconds = eventStartDatetime.atZone(ZoneId.of(eventEntity.getTimeZone())).toEpochSecond();
        if (enableEndTimestamp>epochSeconds)
            throw new BusinessException("No puedo programar para fechas despues del evento.");

        TicketType ticketType = new TicketType();
        ticketType.setEventId(eventId);

        if (timestamp == null || integer == null)
            throw new BusinessException("El ticket requiere la configuracion de validación.");

        ticketType.setValidFromTimestamp(timestamp);
        ticketType.setValidFromDatetime(EpochUtils.epochToLocalDateTime(timestamp));
        long validUntilDateTime = timestamp + (integer * 3600);
        ticketType.setValidUntilTimestamp(validUntilDateTime);
        ticketType.setValidUntilDatetime(EpochUtils.epochToLocalDateTime(validUntilDateTime));

        return ticketType;
    }

    public List<TicketType> getByEventId(Long eventId, String userId){
        log.info("[getByEventId] Starting EventId - {} ", eventId);
        var listEntity = ticketTypeRepository.findByEventIdAndOwnerUserId(eventId);

        for (TicketType entity : listEntity) {
            if (entity != null) {
                if (entity.isActive()) entity.setStatus(StatusEnum.ACTIVE.getCode());
                else entity.setStatus(StatusEnum.INACTIVE.getCode());

                if (EpochUtils.inSeconds() > entity.getEnableEndTimestamp()) entity.setStatus("EE");
                if (EpochUtils.inSeconds() < entity.getEnableStartTimestamp()) entity.setStatus("ES");
            }
        }

        return listEntity;
    }

    public TicketType getByTicketTypeId(Long eventId, Long ticketTypeId){
        log.info("[getByTicketTypeId] Starting EventId - {} ", eventId);
        return ticketTypeRepository.findById(ticketTypeId).get();
    }

    public List<TicketDTO> getEnabledTickets(Long eventId){
        log.info("[getEnabledTickets] Starting EventId - {} - {}", eventId, Instant.now().getEpochSecond());

        var ticketEntities = ticketTypeRepository.findByEventIdAndEnableAndBetweenEnableDate(
                eventId,
                true,
                Instant.now().getEpochSecond());

        List<TicketDTO> dtos = new ArrayList<>();
        for (var entity : ticketEntities) {
            log.debug("[getEnabledTickets] ticketEntity - {}", entity);
            var dto = new TicketDTO(
                    entity.getId(),
                    entity.getName(),
                    entity.getDescription(),
                    entity.getPrice(),
                    entity.getPurchaseQuantityLimit(),
                    entity.getSeatsPerGroup()
            );
            dtos.add(dto);
        }

        return dtos;
    }
}
