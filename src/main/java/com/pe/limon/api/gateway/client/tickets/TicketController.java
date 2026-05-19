package com.pe.limon.api.gateway.client.tickets;

import com.pe.limon.api.transactions.tickets.bussiness.QrService;
import com.pe.limon.api.transactions.tickets.bussiness.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("client/tickets")
@Slf4j
public class TicketController {

    private final QrService qrService;
    private final TicketService ticketService;

    @GetMapping("/mine")
    public ResponseEntity<?> myTickets(
            @RequestAttribute String userId)
    {
        log.debug("[myTickets] Starting controller {}", userId);
        return ResponseEntity.ok(ticketService.getByOwnerUserId(userId));
    }


    @GetMapping("/events/{eventId}/file")
    public ResponseEntity<FileSystemResource> getImage(
            @PathVariable Long eventId,
            @RequestAttribute String userId)
    {
        FileSystemResource fileBytes = qrService.getFileQr(userId,eventId);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(fileBytes);
    }
}
