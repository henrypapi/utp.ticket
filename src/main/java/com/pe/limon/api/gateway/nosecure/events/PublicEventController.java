package com.pe.limon.api.gateway.nosecure.events;


import com.pe.limon.api.core.utils.exception.BusinessException;
import com.pe.limon.api.core.utils.file.FileUtil;
import com.pe.limon.api.gateway.nosecure.events.get.EventDTO;
import com.pe.limon.api.gateway.nosecure.events.get.EventInfoDTO;
import com.pe.limon.api.transactions.tickets.bussiness.TypeManager;
import com.pe.limon.api.transactions.events.business.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public/events")
@Slf4j
public class PublicEventController {

    @Value("${application.images.directory.event}")
    private String pathEventImages;

    private final EventService eventService;
    private final TypeManager typeManager;

    @GetMapping("/image/{filename}")
    public ResponseEntity<FileSystemResource> getImage(@PathVariable String filename) {
        log.debug("getImage {}", filename);
        File file = FileUtil.getFileFromDirectory(pathEventImages, filename);

        if (!file.exists() || !file.isFile()) throw new BusinessException("File not found.");
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(new FileSystemResource(file));
    }

    @GetMapping("/{eventId}/ticket-types")
    public ResponseEntity<?> getTickets(
            @PathVariable Long eventId) {
        return ResponseEntity.ok(typeManager.getEnabledTickets(eventId));
    }

    @GetMapping("/feeds")
    public ResponseEntity<List<EventInfoDTO>> getFeeds(){
        log.info("[getFeeds] Start getFeeds");
        return ResponseEntity.ok(eventService.getEventInfo());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventInfoDTO> getById(@PathVariable Long id){
        log.info("[getFeeds] Start getById");
        return ResponseEntity.ok(eventService.getEventInfo(id));
    }
}
