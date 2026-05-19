package com.pe.limon.api.gateway.admin.sales;

import com.google.zxing.WriterException;
import com.pe.limon.api.core.utils.annotation.RequirePermission;
import com.pe.limon.api.core.utils.annotation.RequirePermissions;
import com.pe.limon.api.core.utils.generics.MessageDTO;
import com.pe.limon.api.gateway.admin.events.dto.PageResult;
import com.pe.limon.api.gateway.admin.sales.dto.SaleReponseDTO;
import com.pe.limon.api.gateway.admin.sales.dto.SalesOverviewDTO;
import com.pe.limon.api.gateway.admin.sales.dto.OrderDetailsDTO;
import com.pe.limon.api.transactions.authz.business.permissions.PermissionPolicy;
import com.pe.limon.api.transactions.orders.business.orders.SalesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/admin/events/{eventId}/sales")
@RequiredArgsConstructor
@Slf4j
public class SalesController {

    private final SalesService salesService;

    @RequirePermissions(value = {
            @RequirePermission(module = "sales", actions = {}),
    }, policy = PermissionPolicy.ALL)
    @GetMapping("/{orderId}/voucher")
    public ResponseEntity<FileSystemResource> getImage(
            @PathVariable Long eventId,
            @PathVariable Long orderId,
            @RequestAttribute String userId)
    {
        FileSystemResource fileBytes = salesService.getFile(eventId,orderId);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(fileBytes);
    }

    @RequirePermissions(value = {
            @RequirePermission(module = "sales", actions = {}),
    }, policy = PermissionPolicy.ALL)
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailsDTO> getDetail(
            @PathVariable Long eventId,
            @PathVariable Long orderId,
            @RequestAttribute String userId)
    {
        return ResponseEntity.ok(salesService.getOrderDetailsById(orderId,eventId));
    }

    @RequirePermissions(value = {
            @RequirePermission(module = "sales", actions = {}),
    }, policy = PermissionPolicy.ALL)
    @GetMapping
    public ResponseEntity<PageResult<SaleReponseDTO>> getSales(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String orderStatus,
            @RequestAttribute String userId) {
        log.info("[getSales] User Id: {} - Event Id: {} - OrderStatus {} - Page: {} - Size: {}", userId, eventId,orderStatus, page, size);
       return ResponseEntity.ok(salesService.getSalesByEventId(eventId, orderStatus,page, size));
    }

    @RequirePermissions(value = {
            @RequirePermission(module = "sales", actions = {}),
    }, policy = PermissionPolicy.ALL)
    @GetMapping("/overview")
    public ResponseEntity<SalesOverviewDTO> getSalesOverview(
            @PathVariable Long eventId,
            @RequestAttribute String userId) {
        log.info("[getSalesOverview] User Id: {} - Event Id: {}", userId, eventId);
        return ResponseEntity.ok(salesService.getSalesOverviewByEventId(eventId));
    }

    @RequirePermissions(value = {
        @RequirePermission(module = "sales", actions = {}),
    }, policy = PermissionPolicy.ALL)
    @PatchMapping("/{orderId}/{action}")
    public ResponseEntity<?> actionVoucher(
            @PathVariable Long eventId,
            @PathVariable Long orderId,
            @PathVariable String action,
            @RequestAttribute String userId) throws IOException, WriterException {
        log.info("[getOrderDetails] User Id: {} - Event Id: {} - Order Id: {}", userId, eventId, orderId);
        salesService.updateStatus(eventId,orderId,action, userId);
        return ResponseEntity.ok(new MessageDTO("ok"));
    }
}