package com.pe.limon.api.gateway.admin.promoter;

import com.pe.limon.api.core.utils.annotation.RequirePermission;
import com.pe.limon.api.core.utils.annotation.RequirePermissions;
import com.pe.limon.api.gateway.admin.events.dto.PageResult;
import com.pe.limon.api.gateway.admin.promoter.dto.CreatePromoterDTO;
import com.pe.limon.api.gateway.admin.promoter.dto.UpdatePromoterDTO;
import com.pe.limon.api.transactions.authz.business.permissions.PermissionPolicy;
import com.pe.limon.api.transactions.authz.business.permissions.PermissionService;
import com.pe.limon.api.transactions.authz.repository.ProfileRepository;
import com.pe.limon.api.transactions.promoters.bussiness.PromoterService;
import com.pe.limon.api.transactions.promoters.bussiness.entity.PromoterEntity;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin/events/{eventId}/promoters")
@RequiredArgsConstructor
public class PromoterController {
    private final ProfileRepository profileRepository;
    private final PromoterService service;
    private final PermissionService permissionService;


    @GetMapping
    public ResponseEntity<PageResult<PromoterEntity>> getPromoters(
            @PathVariable Long eventId,
            @RequestParam() int page,
            @RequestParam() int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String code,
            @RequestAttribute String userId) {

        if (!permissionService.can(eventId, userId, "promoters")){
            var profile = profileRepository.findById(userId);
            if (profile.isEmpty()) return ResponseEntity.notFound().build();
            username = profile.get().getUsername();
        }
        
        PageResult<PromoterEntity> result = service.findByEventId(eventId, username, code, page, size);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/{code}")
    public ResponseEntity<PromoterEntity> getPromoterDetail(
            @RequestAttribute String userId,
            @PathVariable Long eventId,
            @PathVariable String code) {
        if (permissionService.can(eventId,userId,"promoters")) userId = null;
        else {
            var profile = profileRepository.findById(userId);
            if (profile.isEmpty()) return ResponseEntity.notFound().build();
        }
        PromoterEntity result = service.findByEventIdAndCode(userId,eventId, code);
        if (result == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(result);
    }

    @RequirePermissions(value = {
            @RequirePermission(module = "promoter", actions = {}),
    }, policy = PermissionPolicy.ALL)
    @PostMapping
    public ResponseEntity<Void> createPromoter(
            @RequestAttribute String userId,
            @PathVariable Long eventId,
            @RequestBody CreatePromoterDTO dto) {

        service.save(eventId, dto, userId);
        return ResponseEntity.ok().build();
    }

    @RequirePermissions(value = {
            @RequirePermission(module = "promoter", actions = {}),
    }, policy = PermissionPolicy.ALL)
    @PutMapping("/{promoterId}")
    public ResponseEntity<Void> updatePromoter(
            @RequestAttribute String userId,
            @PathVariable Long eventId,
            @PathVariable Long promoterId,
            @RequestBody UpdatePromoterDTO dto) {

        service.update(eventId, promoterId, dto, userId);
        return ResponseEntity.ok().build();
    }
}
