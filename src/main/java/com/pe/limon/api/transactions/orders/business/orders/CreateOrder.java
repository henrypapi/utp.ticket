package com.pe.limon.api.transactions.orders.business.orders;

import com.pe.limon.api.core.utils.exception.BusinessException;
import com.pe.limon.api.core.utils.conversor.EpochUtils;
import com.pe.limon.api.transactions.tickets.repository.entity.TicketType;
import com.pe.limon.api.transactions.orders.business.orders.model.CreateOrderOut;
import com.pe.limon.api.transactions.orders.business.orders.model.OrderStatusEnum;
import com.pe.limon.api.transactions.orders.repository.OrderItemRepository;
import com.pe.limon.api.transactions.orders.repository.OrderRepository;
import com.pe.limon.api.transactions.orders.repository.entity.OrderEntity;
import com.pe.limon.api.transactions.orders.repository.entity.OrderItemEntity;
import com.pe.limon.api.transactions.tickets.repository.TicketTypeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateOrder {
    private final OrderRepository orderRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final OrderItemRepository orderItemRepository;

    public CreateOrderOut execute(String userId,
                                  Map<Long, TicketType> ticketTypeMap,
                                  long eventId,
                                  String paymentRef,
                                  Map<Long, Integer> itemsQuantity, boolean processorPayment){
        log.info("Executing CreateOrder");
        log.info("processoPayment {}", processorPayment);
        Long currentTimestamp = EpochUtils.inSeconds();

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalQuantity = BigDecimal.ZERO;
        int count = 1;

        var items = new ArrayList<OrderItemEntity>();
        Map<Long,OrderItemEntity>  mapOrderItems = new HashMap<>();

        for (Map.Entry<Long, Integer> entry : itemsQuantity.entrySet()) {
            Long ticketTypeId = entry.getKey();
            Integer quantity = entry.getValue();
            var ticketType = ticketTypeMap.get(ticketTypeId);
            OrderItemEntity orderItem = new OrderItemEntity();
            orderItem.setUnitPrice(ticketType.getPrice());

            BigDecimal subtotal = orderItem.getUnitPrice().multiply(BigDecimal.valueOf(quantity));
            orderItem.setSubtotal(subtotal);
            orderItem.setQuantity(quantity);
            orderItem.setTicketType(ticketType);
            orderItem.setProductId(ticketTypeId);
            orderItem.setProductType("ticket");

            totalAmount = totalAmount.add(subtotal);
            totalQuantity = totalQuantity.add(BigDecimal.valueOf(quantity));
            int updReserved = ticketTypeRepository.reserveStock(ticketTypeId, quantity);
            if (updReserved == 0) throw new BusinessException("Stock insuficiente para ticketTypeId: " + ticketType.getName());
            items.add(orderItem);
            mapOrderItems.put(ticketTypeId,orderItem);
            count++;
        }

        OrderEntity order = new OrderEntity();
        order.setUserId(userId);
        order.setEventId(eventId);
        order.setTotalQuantity(totalQuantity.intValue());
        order.setTotalAmount(totalAmount);
        order.setRegisteredDatetime(LocalDateTime.now());
        order.setRegisteredTimestamp(currentTimestamp);
        order.setItems(items);
        order.setStatus(OrderStatusEnum.CREATED.getCode());

        var commissionPercent = 0;
        var commissionPercentDec = 0.00;
        if (processorPayment) {
            commissionPercent=8;
            commissionPercentDec=0.08;
        } else {
            order.setStatus(OrderStatusEnum.WAITING_PROCESSOR.getCode());
            order.setPaymentReference(paymentRef);
        }

        order.setCommissionPercent(commissionPercent);
        order.setCommissionAmount(order.getTotalAmount().multiply(BigDecimal.valueOf(commissionPercentDec)));
        order.setNetAmount(order.getTotalAmount().subtract(order.getCommissionAmount()));

        if (totalAmount.compareTo(BigDecimal.ZERO) == 0) order.setStatus(OrderStatusEnum.COMPLETED.getCode());

        Long orderId = orderRepository.insert(order);

        if (orderId == null || orderId == 0) {
            log.error("[execute] User executor {} in count item {}", userId, count);
            log.error("[execute] Order : {}", order);
            log.error("[execute] Error to insert ticket: {}", orderId);

            throw new BusinessException("Error al generar la orden.");
        }

        for (var item : items){
            item.setOrderId(orderId);
            long itemId = orderItemRepository.insert(item);
            item.setId(itemId);
        }
        return new CreateOrderOut(mapOrderItems, order);
    }
}
