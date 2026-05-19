package com.pe.limon.api.gateway.client.order;

import com.pe.limon.api.core.utils.annotation.RequirePermission;
import com.pe.limon.api.core.utils.annotation.RequirePermissions;
import com.pe.limon.api.gateway.admin.sales.dto.OrderDetailsDTO;
import com.pe.limon.api.transactions.authz.business.permissions.PermissionPolicy;
import com.pe.limon.api.transactions.orders.business.orders.SalesService;
import com.pe.limon.api.transactions.tickets.bussiness.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("client/orders")
@Slf4j
public class OrderClientController {

    private final SalesService  salesService;
    private final TicketService ticketService;

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailsDTO> getOrderDetails(
            @PathVariable Long orderId,
            @RequestAttribute String userId) {
        log.info("[getOrderDetails] User Id: {} - Order Id: {}", userId, orderId);
        return ResponseEntity.ok(salesService.getOrderDetailsById(orderId, userId));
    }

    @GetMapping("/{orderId}/voucher")
    public ResponseEntity<FileSystemResource> getImage(
            @PathVariable Long orderId,
            @RequestAttribute String userId)
    {
        FileSystemResource fileBytes = salesService.getClientFile(orderId,userId);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(fileBytes);
    }

    @GetMapping("/{orderId}/tickets")
    public ResponseEntity<?> getOrderTickets(
            @PathVariable Long orderId,
            @RequestAttribute String userId) {
        log.info("[getOrderTickets] User Id: {} - Order Id: {}", userId, orderId);
        return ResponseEntity.ok(ticketService.getByOwnerUserIdAndOrderId(userId,orderId));
    }
}
