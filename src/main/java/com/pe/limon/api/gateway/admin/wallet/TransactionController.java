package com.pe.limon.api.gateway.admin.wallet;

import com.pe.limon.api.gateway.admin.events.dto.PageResult;
import com.pe.limon.api.gateway.admin.wallet.dto.TransactionDto;
import com.pe.limon.api.transactions.wallet.business.TransactionService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<PageResult<TransactionDto>> findAll(
            @RequestParam int size,
            @RequestParam int page,
            @RequestAttribute String userId
    ) {
        return ResponseEntity.ok(transactionService.getTransactions(userId,size, page));
    }
}