package com.pe.limon.api.transactions.orders.business.checkout.models;

import com.pe.limon.api.transactions.orders.repository.entity.OrderEntity;
import com.pe.limon.api.transactions.wallet.repository.entity.VoucherEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class CheckoutContext {
    private OrderEntity order;
}
