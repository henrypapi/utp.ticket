package com.pe.limon.api.gateway.admin.tickets;

import com.pe.limon.api.core.utils.generics.MessageDTO;
import com.pe.limon.api.gateway.admin.tickets.dto.types.CreateDTO;
import com.pe.limon.api.gateway.admin.tickets.dto.types.UpdateDTO;
import com.pe.limon.api.transactions.tickets.bussiness.TypeManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/events/{eventId}/tickets/types")
public class AdminTicketTypeController {
    private final TypeManager typeManager;

    @PostMapping
    public ResponseEntity<?> addTicketType(@Valid @RequestBody CreateDTO createDTO, @RequestAttribute String userId) {
        typeManager.addTicketToEvent(createDTO, userId);
        return ResponseEntity.ok(new MessageDTO("ok"));
    }

    @PutMapping("/{ticketTypeId}")
    public ResponseEntity<?> putTicketType(
            @Valid @RequestBody UpdateDTO updateDTO,
            @PathVariable Long ticketTypeId,
            @RequestAttribute String userId) {
        typeManager.updateTicketType(updateDTO, ticketTypeId,userId);
        return ResponseEntity.ok(new MessageDTO("ok"));
    }

    @DeleteMapping("/{typeId}")
    public ResponseEntity<?> deleteTicketType(@RequestAttribute String userId) {
        return ResponseEntity.ok(new MessageDTO("ok"));
    }


    @GetMapping
    public ResponseEntity<?> getByEventId(
            @PathVariable Long eventId,
            @RequestAttribute String userId) {
        return ResponseEntity.ok(typeManager.getByEventId(eventId,userId));
    }

    @GetMapping("/{ticketTypeId}")
    public ResponseEntity<?> getById(
            @PathVariable Long eventId,
            @PathVariable Long ticketTypeId,
            @RequestAttribute String userId) {
        return ResponseEntity.ok(typeManager.getByTicketTypeId(eventId,ticketTypeId));
    }

}
