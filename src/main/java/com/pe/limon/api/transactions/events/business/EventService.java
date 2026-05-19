package com.pe.limon.api.transactions.events.business;

import com.pe.limon.api.core.utils.conversor.EpochUtils;
import com.pe.limon.api.core.utils.enums.StatusEnum;
import com.pe.limon.api.core.utils.exception.NotFoundException;
import com.pe.limon.api.gateway.admin.events.dto.PageResult;
import com.pe.limon.api.gateway.admin.events.dto.create.CreateDTO;
import com.pe.limon.api.core.utils.exception.BusinessException;
import com.pe.limon.api.core.utils.generics.GeoPoint;
import com.pe.limon.api.core.utils.file.FileUtil;
import com.pe.limon.api.gateway.nosecure.events.get.EventDTO;
import com.pe.limon.api.gateway.nosecure.events.get.EventInfoDTO;
import com.pe.limon.api.gateway.client.users.profile.dto.ProfileDTO;
import com.pe.limon.api.transactions.events.repository.event.EventEntity;
import com.pe.limon.api.transactions.events.repository.EventRepository;
import com.pe.limon.api.transactions.tickets.repository.TicketTypeRepository;
import com.pe.limon.api.transactions.authz.business.profile.ProfileService;
import com.pe.limon.api.transactions.tickets.repository.entity.TicketType;
import com.pe.limon.api.transactions.wallet.repository.WalletRepository;
import com.pe.limon.api.transactions.wallet.repository.entity.WalletEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    @Value("${application.images.directory.event}")
    private String imageUploadPath;
    @Value("${application.images.url.image-event}")
    private String urlBaseImage;
    private final EventRepository eventRepository;
    private final ProfileService profileService;
    private final TicketTypeRepository ticketTypeRepository;
    private final WalletRepository walletRepository;
    @Transactional
    public void register(CreateDTO dto, String userId) {
        log.info("[register] Start register {} ", dto.toString());

        if (!FileUtil.isValidFile(dto.basicInfo().image(), FileUtil.ALLOWED_EXTENSIONS_IMAGE))
            throw new BusinessException("Archivo de imagen inválido");

        var entity = mapCreateToEntity(dto);
        entity.setOwnerUserId(userId);
        entity.setRegisteredDatetime(LocalDateTime.now());
        entity.setRegisteredTimestamp(System.currentTimeMillis());
        entity.setStatus(StatusEnum.ACTIVE.getCode());

        if (entity.getPaymentMode().equals(PaymentModeEnum.PROCESSOR.getCode())){
            log.info("[register] payment mode PROCESSOR");
           if (!walletRepository.existsByOwnerUserId(userId)){
               log.info("[register] wallet not found");
               WalletEntity wallet = new WalletEntity();
               wallet.setOwnerUserId(userId);
               wallet.setId(UUID.randomUUID().toString().substring(0, 12));
               wallet.setCurrency("PEN");
               wallet.setBalanceAvailable(BigDecimal.ZERO);
               wallet.setBalanceHeld(BigDecimal.ZERO);
               wallet.setStatus(StatusEnum.ACTIVE.getCode());
               wallet.setAllowWithdrawals(true);
               wallet.setRegisteredDatetime(LocalDateTime.now());
               wallet.setRegisteredTimestamp(EpochUtils.inSeconds());
               walletRepository.insert(wallet);
           }
        }

        String fileName = "event_" +
            System.currentTimeMillis() +
            "." +
            Objects
                .requireNonNull(dto.basicInfo().image().getOriginalFilename())
                .substring(dto.basicInfo().image().getOriginalFilename().lastIndexOf('.') + 1);

        try {
            FileUtil.saveFile(dto.basicInfo().image(), fileName, imageUploadPath);
            entity.setImg(fileName);
            eventRepository.insert(entity);
        } catch (IOException e) {
            throw new BusinessException("No se pudo guardar la imagen del evento" + e.getMessage());
        }
    }

    @Transactional
    public void update(Long eventId,CreateDTO dto, String userId) {
        log.info("[update] Start update {} ", dto.toString());
        var event = eventRepository.findById(eventId);
        log.info("[event] before {} ",event.getStartDatetime());
        if (dto.basicInfo().image() != null) {
            log.debug("Imagen enviada, realizando validacion");
            if (!FileUtil.isValidFile(dto.basicInfo().image(), FileUtil.ALLOWED_EXTENSIONS_IMAGE)) {
                throw new BusinessException("Archivo de imagen inválido");
            }
        }

        var entity = mapCreateToEntity(dto);
        log.info("[event] before {} ",entity.getStartDatetime());

        entity.setId(eventId);
        entity.setOwnerUserId(userId);
        if (dto.basicInfo().image() != null) {
            log.debug("Imagen enviada, guardando");
            String fileName = "event_" +
                    System.currentTimeMillis() +
                    "." +
                    Objects.requireNonNull(
                                    dto.basicInfo().image().getOriginalFilename()
                            )
                            .substring(dto.basicInfo().image().getOriginalFilename().lastIndexOf('.') + 1);

            try {
                FileUtil.saveFile(dto.basicInfo().image(), fileName, imageUploadPath);
                entity.setImg(fileName);
            } catch (IOException e) {
                throw new BusinessException("No se pudo guardar la imagen del evento" + e.getMessage());
            }
        }

        if (!event.getStartDatetime().truncatedTo(ChronoUnit.MINUTES)
                .equals(entity.getStartDatetime().truncatedTo(ChronoUnit.MINUTES))){
            List<TicketType> types = ticketTypeRepository.findByEventIdAndOwnerUserId(eventId);

            ZoneId zone = ZoneId.of(event.getTimeZone());
            LocalDate newDate = entity.getStartDatetime().toLocalDate();

            for (var type : types) {
                LocalDateTime fromOrig = EpochUtils.epochToLocalDateTimeWithZone(type.getValidFromTimestamp(), zone);
                LocalDateTime toOrig   = EpochUtils.epochToLocalDateTimeWithZone(type.getValidUntilTimestamp(), zone);

                Duration duration = Duration.between(fromOrig, toOrig);

                // (opcional) si por data mala te sale duración <= 0, puedes forzar regla de cruce de medianoche:
                // if (!duration.isPositive()) duration = duration.plusDays(1);

                LocalDateTime fromNew = LocalDateTime.of(newDate, fromOrig.toLocalTime());
                LocalDateTime toNew   = fromNew.plus(duration);

                type.setValidFromTimestamp(EpochUtils.localDateTimeToEpochSeconds(fromNew, zone));
                type.setValidUntilTimestamp(EpochUtils.localDateTimeToEpochSeconds(toNew, zone));
                type.setValidFromDatetime(fromNew);
                type.setValidUntilDatetime(toNew);

                ticketTypeRepository.updateValidTicket(type);
            }
        }
        eventRepository.update(entity);
    }

    public PageResult<EventEntity> getMyEvents(String userId, int page, int size){
        log.info("[getUserEvents] Starting process: {}", userId);
        return eventRepository.findByOwnerOrAdminUser(userId,page,size);
    }

    public EventEntity getUserEvent(String userId, long eventId){
        log.info("[getUserEvent] Starting process: {}", userId);
        var entity = eventRepository.findByOwnerUserIdAndEventId(userId,eventId);

        if (entity == null) throw new BusinessException("Evento no encontrado.");

        if (entity.getLocation() != null) entity.setGeoLocation(new GeoPoint(entity.getLocation()));

        entity.setImg(urlBaseImage+entity.getImg());
        log.info("[getUserEvent] Ending process: {}", entity.getId());
        return entity;
    }

    public EventInfoDTO getEventInfo(long eventId){
        log.info("[getEventInfo] Starting process: {}", eventId);
        var entity = eventRepository.findByIdAndStatus(eventId, StatusEnum.ACTIVE.getCode());

        if (entity == null) throw new NotFoundException("Evento no disponible.");

        if (entity.getLocation() != null){
            entity.setGeoLocation(new GeoPoint(entity.getLocation()));
        }

        var profile = profileService.getProfile(entity.getOwnerUserId());

        EventDTO eventDTO = new EventDTO(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getAddress(),
            entity.getGeoLocation(),
            urlBaseImage+entity.getImg(),
            entity.getStartDatetime().toLocalDate(),
            entity.getStartDatetime().toLocalTime(),
            entity.getTimeZone(),
            entity.getQrEnabled(),
            entity.getTerms(),
            entity.getPaymentMode(),
                entity.getPaymentComment(),
                entity.getRedirectAfterPay()
        );

        ProfileDTO profileDTO = new ProfileDTO(
                profile.getUsername(),
                profile.getProfileImage()
        );

        log.info("[getEventInfo] Ending process: {}", entity);
        return new EventInfoDTO(
                eventDTO,
                profileDTO
        );
    }


    public List<EventInfoDTO> getEventInfo(){
        log.info("[getEventInfo] Starting process");
        var list = eventRepository.findAll(LocalDateTime.now(ZoneId.of("America/Lima")));
        List<EventInfoDTO> eventInfoDTOList = new ArrayList<>();
        for (var item  : list){
            if (item.getLocation() != null) item.setGeoLocation(new GeoPoint(item.getLocation()));

            var profile = profileService.getProfile(item.getOwnerUserId());

            EventDTO eventDTO = new EventDTO(
                    item.getId(),
                    item.getName(),
                    item.getDescription(),
                    item.getAddress(),
                    item.getGeoLocation(),
                    urlBaseImage+item.getImg(),
                    item.getStartDatetime().toLocalDate(),
                    item.getStartDatetime().toLocalTime(),
                    item.getTimeZone(),
                    item.getQrEnabled(),
                    item.getTerms(),
                    item.getPaymentMode(),
                    item.getPaymentComment(),
                    item.getRedirectAfterPay()
            );

            ProfileDTO profileDTO = new ProfileDTO(
                    profile.getUsername(),
                    profile.getProfileImage()
            );

            log.info("[getEventInfo] Ending process: {}", item);
            eventInfoDTOList.add(new EventInfoDTO(
                    eventDTO,
                    profileDTO
            ));
        }
        return eventInfoDTOList;
    }


    public EventDTO getOverview(long eventId){
        log.info("[getOverview] Starting process: {}", eventId);
        var entity = eventRepository.findById(eventId);

        if (entity == null) throw new NotFoundException("Evento no encontrado.");

        if (entity.getLocation() != null) entity.setGeoLocation(new GeoPoint(entity.getLocation()));

        return new EventDTO(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getAddress(),
            entity.getGeoLocation(),
                urlBaseImage+entity.getImg(),
            entity.getStartDatetime().toLocalDate(),
            entity.getStartDatetime().toLocalTime(),
            entity.getTimeZone(),
            entity.getQrEnabled(),
            entity.getTerms(),
            entity.getPaymentMode(),
            entity.getPaymentComment(),
                entity.getRedirectAfterPay()
        );
    }

    private EventEntity mapCreateToEntity(CreateDTO dto){
        EventEntity entity = new EventEntity();
        entity.setName(dto.basicInfo().eventName());
        entity.setDescription(dto.basicInfo().eventDescription());
        entity.setTypeId(dto.basicInfo().typeId());

        //SCHEDULE MAP
        entity.setPaymentMode(dto.basicInfo().paymentMode());
        entity.setQrEnabled(dto.basicInfo().qrEnabled());
        entity.setImg(null);
        LocalDateTime startDatetime = LocalDateTime.of(dto.basicInfo().startDate(), dto.basicInfo().startTime());
        entity.setStartDatetime(startDatetime);

        //LOCATION
        entity.setAddress(dto.location().address());
        GeoPoint point = new GeoPoint(dto.location().latitude(), dto.location().longitude());
        entity.setLocation(point.toString());
        entity.setCountry("PE");
        entity.setTimeZone("America/Lima");
        entity.setMetadata(dto.location().metadata());
        entity.setRedirectAfterPay(dto.basicInfo().redirectAfterPay());
        entity.setPaymentComment(dto.basicInfo().paymentComment());
        if (dto.terms().terms()!=null) entity.setTerms(dto.terms().terms());

        return entity;
    }
}
