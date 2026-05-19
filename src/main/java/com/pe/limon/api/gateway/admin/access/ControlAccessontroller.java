package com.pe.limon.api.gateway.admin.access;

import com.pe.limon.api.gateway.admin.access.dto.AccessPassDTO;
import com.pe.limon.api.gateway.admin.access.dto.AccessPassFiltersDTO;
import com.pe.limon.api.gateway.admin.access.dto.ScanAndAdmitRequestDTO;
import com.pe.limon.api.gateway.admin.access.dto.ScanAndAdmitResponseDTO;
import com.pe.limon.api.gateway.admin.events.dto.PageResult;
import com.pe.limon.api.transactions.tickets.bussiness.ControlAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/events/{eventId}/access")
@RequiredArgsConstructor
@Slf4j
public class ControlAccessontroller {

    private final ControlAccessService controlAccessService;

    @GetMapping("/passes")
    public ResponseEntity<PageResult<AccessPassDTO>> getAccessPasses(
            @PathVariable Long eventId,
            @ModelAttribute AccessPassFiltersDTO filters,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestAttribute String userId) {

        log.info("[getAccessPasses] EventId: {}, UserId: {}, Filters: firstname={}, lastname={}, username={}, email={}, admissionStatus={}, page={}, size={}",
                eventId, userId, filters.firstname(), filters.lastname(), filters.username(), filters.email(), filters.admissionStatus(), page, size);

        PageResult<AccessPassDTO> result = controlAccessService.getAccessPassesByEventId(
                eventId, filters, page, size);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/check-in/{accessId}")
    public ResponseEntity<ScanAndAdmitResponseDTO> checkIn(
            @PathVariable Long eventId,
            @PathVariable Long accessId,
            @RequestAttribute String userId) {

        ScanAndAdmitResponseDTO result = controlAccessService.checkIn(accessId, userId, eventId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/scan-and-admit")
    public ResponseEntity<ScanAndAdmitResponseDTO> scanAndAdmit(
            @PathVariable Long eventId,
            @RequestBody ScanAndAdmitRequestDTO request,
            @RequestAttribute String userId) {

        log.info("[scanAndAdmit]  UserId: {}", userId);
        ScanAndAdmitResponseDTO result = controlAccessService.scanAndAdmit(userId, eventId, request);

        return ResponseEntity.ok(result);
    }
}
