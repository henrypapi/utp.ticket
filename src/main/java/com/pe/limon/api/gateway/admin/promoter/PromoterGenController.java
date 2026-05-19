package com.pe.limon.api.gateway.admin.promoter;

import com.pe.limon.api.transactions.promoters.bussiness.PromoterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/promoters")
@RequiredArgsConstructor
public class PromoterGenController {
    private final PromoterService service;

    @GetMapping("/my-events")
    public ResponseEntity<?> getEventsAsPromoter(
            @RequestAttribute String userId) {
        return ResponseEntity.ok(service.getEventByUserId(userId));
    }
}
