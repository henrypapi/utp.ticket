package com.pe.limon.api.transactions.orders.business.checkout.models;

import com.pe.limon.api.transactions.wallet.repository.entity.TransactionEntity;
import com.pe.limon.api.transactions.wallet.repository.entity.VoucherEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponse {
    private TransactionEntity transaction;
    private VoucherEntity voucherEntity;
}
