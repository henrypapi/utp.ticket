package com.pe.limon.api.gateway.client.events;

import com.pe.limon.api.gateway.nosecure.events.get.EventDTO;
import com.pe.limon.api.transactions.events.business.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("client/events")
@Slf4j
public class ClientEventController {
    private final EventService eventService;

    @GetMapping("{id}/overview")
    public ResponseEntity<EventDTO> getOverview(@PathVariable Long id){
        log.info("[getOverview] Start getOverview");
        return ResponseEntity.ok(eventService.getOverview(id));
    }
}
