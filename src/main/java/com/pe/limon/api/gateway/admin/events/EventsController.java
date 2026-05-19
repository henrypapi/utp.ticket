package com.pe.limon.api.gateway.admin.events;

import com.pe.limon.api.core.utils.generics.MessageDTO;
import com.pe.limon.api.transactions.events.repository.event.EventEntity;
import com.pe.limon.api.gateway.admin.events.dto.PageResult;
import com.pe.limon.api.gateway.admin.events.dto.create.CreateDTO;
import com.pe.limon.api.transactions.events.business.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Slf4j
public class EventsController {
    private final EventService eventService;

    @PostMapping
    public ResponseEntity<?> postEvent(@Valid @ModelAttribute CreateDTO dto, @RequestAttribute String userId){
        eventService.register(dto, userId);
        return ResponseEntity.ok(new MessageDTO("Evento creado correctamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> putEvent(
            @PathVariable Long id,
            @ModelAttribute CreateDTO dto,
            @RequestAttribute String userId){
        eventService.update(id,dto,userId);
        return ResponseEntity.ok(new MessageDTO("Evento creado correctamente"));
    }

    @GetMapping("/all")
    public ResponseEntity<PageResult<EventEntity>> getAll(
            @RequestParam int size,
            @RequestParam int page,
            @RequestAttribute String userId){
        log.info("[getAll] User Id: {} - {} - {}", userId, size, page);
        return ResponseEntity.ok(eventService.getMyEvents(userId, page,size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventEntity> getAll(
            @PathVariable Long id,
            @RequestAttribute String userId){
        log.info("[getAll] User Id: {}", userId);
        return ResponseEntity.ok(eventService.getUserEvent(userId, id));
    }
}
