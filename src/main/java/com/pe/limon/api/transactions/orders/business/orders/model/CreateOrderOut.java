package com.pe.limon.api.transactions.orders.business.orders.model;

import com.pe.limon.api.transactions.orders.repository.entity.OrderEntity;
import com.pe.limon.api.transactions.orders.repository.entity.OrderItemEntity;
import lombok.Data;

import java.util.Map;

@Data
public class CreateOrderOut {
    private Map<Long, OrderItemEntity> mapOrderItems;
    private OrderEntity order;

    public CreateOrderOut(Map<Long, OrderItemEntity> mapOrderItems, OrderEntity orderEntity) {
        this.mapOrderItems = mapOrderItems;
        this.order = orderEntity;
    }

}
