package com.pe.limon.api.gateway.admin.wallet;

import com.pe.limon.api.core.utils.generics.MessageDTO;
import com.pe.limon.api.gateway.admin.wallet.dto.WalletRequest;
import com.pe.limon.api.transactions.wallet.business.WalletService;
import com.pe.limon.api.transactions.wallet.repository.entity.WalletEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/wallets/")
@RequiredArgsConstructor
@Slf4j
public class WalletController {
    private final WalletService walletService;


    @GetMapping("/overview")
    public ResponseEntity<WalletEntity> getOverview(
            @RequestAttribute String userId) {
        log.info("[getOverview] User Id: {}", userId);
        return ResponseEntity.ok(walletService.getByOwnerUserId(userId));
    }

    @PostMapping("/requests")
    public ResponseEntity<?> withdrawal(
            @RequestBody WalletRequest walletRequest,
            @RequestAttribute String userId) {
        log.info("[getOverview] User Id: {}", userId);
        walletService.generateRequest(userId,walletRequest);
        return ResponseEntity.ok(new MessageDTO("ok"));
    }

}
