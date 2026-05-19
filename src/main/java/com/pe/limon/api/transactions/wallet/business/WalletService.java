package com.pe.limon.api.transactions.wallet.business;


import com.pe.limon.api.core.utils.conversor.EpochUtils;
import com.pe.limon.api.core.utils.conversor.JsonUtils;
import com.pe.limon.api.core.utils.exception.BusinessException;
import com.pe.limon.api.gateway.admin.wallet.dto.WalletRequest;
import com.pe.limon.api.transactions.wallet.repository.WalletRepository;
import com.pe.limon.api.transactions.wallet.repository.WalletRequestRepository;
import com.pe.limon.api.transactions.wallet.repository.entity.TransactionEntity;
import com.pe.limon.api.transactions.wallet.repository.entity.WalletEntity;
import com.pe.limon.api.transactions.wallet.repository.entity.WalletRequestEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletRequestRepository walletRequestRepository;

    public WalletEntity getByOwnerUserId(String ownerUserId) {
        return walletRepository.finByOwnerUserId(ownerUserId);
    }

    public void refreshBalance(Long eventId, List<TransactionEntity> newTxns) {
        log.info("[refreshBalance] Starting: Event {} newTxns {}", eventId, newTxns );

        var newWallet = walletRepository.finByEventIdForUpdate(eventId);
        log.info("[refreshBalance] Starting: newWallet {}",newWallet);
        BigDecimal available = safe(newWallet.getBalanceAvailable());
        BigDecimal held = safe(newWallet.getBalanceHeld());

        TransactionEntity lastTxn = null;

        for (TransactionEntity tx : newTxns) {
            if (tx == null) continue;

            BigDecimal amount = safe(tx.getAmount());
            if (amount.signum() == 0) continue;

            int sign = operationSign(tx.getOperation()); // +1 o -1
            BigDecimal delta = amount.multiply(BigDecimal.valueOf(sign));

            if (TxStatusEnum.COMPLETED.getCode().equals(tx.getStatus())) available = available.add(delta);
            else if (TxStatusEnum.PENDING.getCode().equals(tx.getStatus())) held = held.add(delta);

            if (lastTxn == null || tx.getUpdatedTimestamp() > lastTxn.getUpdatedTimestamp()) {
                lastTxn = tx;
            }
        }

        WalletEntity refreshed = new WalletEntity();
        refreshed.setId(newWallet.getId());
        refreshed.setBalanceAvailable(available);
        refreshed.setBalanceHeld(held);
        refreshed.setBalanceTotal(available.add(held));
        refreshed.setUpdatedDatetime(LocalDateTime.now());
        refreshed.setUpdatedTimestamp(EpochUtils.inSeconds());

        if (lastTxn != null) {
            refreshed.setLastTxnId(String.valueOf(lastTxn.getId()));
            refreshed.setLastTxnTimestamp(lastTxn.getUpdatedTimestamp());
            refreshed.setLastTxnDatetime(lastTxn.getUpdatedDatetime());
        }

        long updated = walletRepository.updateBalance(refreshed);
        log.info("[refreshBalance] Updated resulted : {}", updated);
    }

    private static BigDecimal safe(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static int operationSign(String operation) {
        if (OperationEnum.CREDIT.getCode().equalsIgnoreCase(operation)) return 1;
        if (OperationEnum.DEBIT.getCode().equalsIgnoreCase(operation)) return -1;
        throw new IllegalArgumentException("Unknown operation: " + operation);
    }

    public void generateRequest(String userId, WalletRequest walletRequest){
        var walletEntity = walletRepository.finByOwnerUserId(userId);

        if (walletEntity.getBalanceTotal().compareTo(BigDecimal.ZERO)==0){
            throw new BusinessException("El monto solicitado supera al monto permitido.");
        }

        WalletRequestEntity walletRequestEntity = new WalletRequestEntity();
        walletRequestEntity.setWalletId(walletEntity.getId());
        walletRequestEntity.setAmount(walletRequest.amount());
        walletRequestEntity.setCurrency("PEN");
        walletRequestEntity.setAttachmentsJson(JsonUtils.convertToJsonString(walletRequest));
        walletRequestEntity.setDescription("Solicitud de retiro");
        walletRequestEntity.setRequestType(OperationEnum.DEBIT.getCode());
        walletRequestEntity.setRegisteredTimestamp(EpochUtils.inSeconds());
        walletRequestEntity.setRegisteredDatetime(LocalDateTime.now());
        walletRequestRepository.inert(walletRequestEntity);
    }
}
