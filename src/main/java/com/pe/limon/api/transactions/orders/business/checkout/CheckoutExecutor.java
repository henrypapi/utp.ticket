package com.pe.limon.api.transactions.orders.business.checkout;

import com.pe.limon.api.core.utils.conversor.EpochUtils;
import com.pe.limon.api.core.utils.enums.StatusEnum;
import com.pe.limon.api.core.utils.exception.BusinessException;
import com.pe.limon.api.gateway.client.checkout.dto.CheckOutResponse;
import com.pe.limon.api.gateway.client.checkout.dto.CheckoutDTO;
import com.pe.limon.api.gateway.client.checkout.dto.ItemsDTO;
import com.pe.limon.api.gateway.client.checkout.dto.VoucherDTO;
import com.pe.limon.api.transactions.authz.repository.UserRepository;
import com.pe.limon.api.transactions.events.business.PaymentModeEnum;
import com.pe.limon.api.transactions.events.repository.EventRepository;
import com.pe.limon.api.transactions.events.repository.event.EventEntity;
import com.pe.limon.api.transactions.orders.business.checkout.models.CheckoutVoucher;
import com.pe.limon.api.transactions.orders.business.checkout.models.TicketValidationResult;
import com.pe.limon.api.transactions.orders.repository.OrderItemRepository;
import com.pe.limon.api.transactions.tickets.repository.entity.TicketType;
import com.pe.limon.api.transactions.orders.business.checkout.models.CheckoutProcessor;
import com.pe.limon.api.transactions.orders.business.orders.CheckoutTransactionManager;
import com.pe.limon.api.transactions.orders.business.orders.model.OrderStatusEnum;
import com.pe.limon.api.transactions.orders.repository.OrderRepository;
import com.pe.limon.api.transactions.orders.repository.entity.OrderEntity;
import com.pe.limon.api.transactions.tickets.repository.TicketTypeRepository;
import com.pe.limon.api.transactions.wallet.business.TxStatusEnum;
import com.pe.limon.api.transactions.wallet.repository.entity.TransactionEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CheckoutExecutor {

    private final ProcessCheckoutFactory processCheckoutFactory;
    private final OrderRepository orderRepository;
    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final UserRepository userRepository;
    private final CheckoutTransactionManager checkoutTransactionManager;
    private final OrderItemRepository orderItemRepository;

    /**
     * Ejecuta el checkout para pagos con procesador
     * (tarjeta, Yape u otros).
     * Flujo:
     * 1. Validaciones generales
     * 2. Creación de orden
     * 3. Generación de tickets
     * 4. Procesamiento de pago
     * 5. Actualización de estados
     *
     * @param userId usuario que realiza la compra
     * @param ip IP del cliente
     * @param dto información del checkout
     * @return respuesta con el id de la orden
     */
    public CheckOutResponse execute(String userId, String ip, CheckoutDTO dto) {

        log.info("[checkout] Inicio userId={} eventId={}", userId, dto.eventId());

        long now = EpochUtils.inSeconds();
        EventEntity event = eventRepository.findByIdAndStatus(dto.eventId(), StatusEnum.ACTIVE.getCode());
        if (event == null)
            throw new BusinessException("Evento no disponible");

        findAndValidateEvent(event, PaymentModeEnum.PROCESSOR.getCode());

        var resultValidation = loadAndValidateTicketTypes(dto.eventId(), now, dto.items());
        var itemsQuantity = resultValidation.itemsQuantity();
        var ticketTypeMap = resultValidation.ticketTypeMap();

        /*Descomentar cuando se puedan asignar usuarios*/
        //validateAssignedUsers(dto.items(), ticketTypeMap);

        var orderCreated = checkoutTransactionManager.createOrder(
                userId,
                ticketTypeMap,
                dto.eventId(),
                itemsQuantity,
                dto.attribution(),
                null,
                true
        );

        var order = orderCreated.getOrder();
        boolean ticketsGenerated = false;
        boolean paid = false;
        String paymentRef = null;
        boolean noPaymentRequired = order.getTotalAmount().floatValue() == 0f;

        try {
            List<Long> ticketIds = checkoutTransactionManager.generateTickets(
                    userId,
                    noPaymentRequired,
                    dto.items(),
                    ticketTypeMap,
                    order.getItems()
            );
            ticketsGenerated = true;

            if (!noPaymentRequired) {
                TransactionEntity tx = processPayment(ip, dto, order);
                paid = TxStatusEnum.COMPLETED.getCode().equals(tx.getStatus());
                paymentRef = tx.getId().toString();
                updateOrderAfterPayment(order, tx);

                if (!paid) return new CheckOutResponse(order.getId());

                checkoutTransactionManager.completeCheckout(
                    userId,
                    dto.eventId(),
                    event.getQrEnabled(),
                    tx,
                    ticketIds,
                    ticketTypeMap,
                    orderCreated
                );

                markOrderCompleted(order.getId(), order.getEventId());
            } else {
                checkoutTransactionManager.completeCheckout(
                    userId,
                    dto.eventId(),
                        event.getQrEnabled(),
                    null,
                    ticketIds,
                    ticketTypeMap,
                    orderCreated
                );
            }

            return new CheckOutResponse(order.getId());

        } catch (Exception e) {
            log.error("[checkout] Error en checkout orderId={}", order.getId(), e);
            handleCheckoutFailure(order, paymentRef, ticketsGenerated, paid, ticketTypeMap);
            return new CheckOutResponse(order.getId());
        }
    }

    /**
     * Ejecuta el checkout mediante voucher.
     * No existe pago en línea, el comprobante queda
     * pendiente de validación administrativa.
     *
     * @param userId usuario que realiza la compra
     * @param ip IP del cliente
     * @param dto información del voucher
     * @return respuesta con el id de la orden
     */
    public CheckOutResponse execute(String userId, String ip, VoucherDTO dto) {

        log.info("[checkout-voucher] Inicio userId={} eventId={} ip={}", userId, dto.getEventId(),ip);

        long now = EpochUtils.inSeconds();

        EventEntity event = eventRepository.findByIdAndStatus(dto.getEventId(), StatusEnum.ACTIVE.getCode());
        if (event == null)
            throw new BusinessException("Evento no disponible");

        findAndValidateEvent(event, PaymentModeEnum.VOUCHER.getCode());

        var resultValidation =
                loadAndValidateTicketTypes(dto.getEventId(), now, dto.getItems());
        var itemsQuantity = resultValidation.itemsQuantity();
        var ticketTypeMap = resultValidation.ticketTypeMap();

        //validateAssignedUsers(dto.getItems(), ticketTypeMap);

        var processCheckout = processCheckoutFactory.processCheckout("voucher");
        var voucherResp = processCheckout.process(new CheckoutVoucher(dto.getVoucher()));

        var orderCreated = checkoutTransactionManager.createOrder(
            userId,
            ticketTypeMap,
            dto.getEventId(),
            itemsQuantity,
            dto.getAttribution(),
            voucherResp.getVoucherEntity().getFileName(),
            false
        );

        var order = orderCreated.getOrder();
        boolean ticketsGenerated = true;

        try {
            checkoutTransactionManager.generateTickets(
                    userId,
                    false,
                    dto.getItems(),
                    ticketTypeMap,
                    order.getItems()
            );
            return new CheckOutResponse(order.getId());
        } catch (Exception e) {
            log.error("[checkout-voucher] Error orderId={}", order.getId(), e);
            handleCheckoutFailure(order, null, ticketsGenerated, false, ticketTypeMap);
            return new CheckOutResponse(order.getId());
        }
    }

    /**
     * Valida la existencia del evento y su modo de pago.
     */
    private void findAndValidateEvent(EventEntity event, String expectedPaymentMode) {

        if (!expectedPaymentMode.equals(event.getPaymentMode()))
            throw new BusinessException("El evento no permite este método de pago");
    }

    /**
     * Obtiene los tickets activos y válidos para el evento.
     */
    private TicketValidationResult loadAndValidateTicketTypes(
            Long eventId,
            long now,
            List<ItemsDTO> items
    ) {
        var ticketTypes = ticketTypeRepository.findByAndEventIdAndIdsEnableTimestampAndActive(
                eventId,
                true,
                now,
                items.stream().map(ItemsDTO::getTicketTypeId).distinct().toList()
        );

        if (ticketTypes.isEmpty())
            throw new BusinessException("Los tickets seleccionados no están disponibles");

        Map<Long, TicketType> ticketTypeMap = ticketTypes.stream()
                .collect(Collectors.toMap(TicketType::getId, t -> t));

        // ✅ construir itemsQuantity en el mismo loop
        Map<Long, Integer> itemsQuantity = new HashMap<>();

        for (ItemsDTO item : items) {
            Long ticketTypeId = item.getTicketTypeId();
            TicketType tt = ticketTypeMap.get(ticketTypeId);

            if (tt == null)
                throw new BusinessException("Ticket no disponible: " + ticketTypeId);

            int assigned = item.getAssignedUsersId() == null ? 0 : item.getAssignedUsersId().size();
            Integer seatsPerGroup = tt.getSeatsPerGroup();

            if (seatsPerGroup == null || seatsPerGroup <= 0)
                throw new BusinessException("Configuración inválida del ticket " + tt.getName() + " (Cupos por ticket)");

            if (assigned != seatsPerGroup)
                throw new BusinessException(
                        "El ticket " + tt.getName() + " requiere " + seatsPerGroup
                                + " asientos por grupo, pero enviaste " + assigned
                );

            // ✅ contar cantidad por ticketTypeId
            itemsQuantity.merge(ticketTypeId, 1, Integer::sum);
        }

        var allItemsEvent = orderItemRepository.findQuantityByProductId(eventId);

        // ✅ validación de límite usando el map (no items)
        for (Map.Entry<Long, Integer> entry : itemsQuantity.entrySet()) {
            Long ticketTypeId = entry.getKey();
            int qty = entry.getValue();
            int qtyPrev = allItemsEvent.getOrDefault(ticketTypeId, 0);

            TicketType tt = ticketTypeMap.get(ticketTypeId);
            if (tt == null)
                throw new BusinessException("Ticket no disponible: " + ticketTypeId);

            Integer qLimit = tt.getPurchaseQuantityLimit();

            if (qLimit == null || qLimit <= 0)
                throw new BusinessException("Configuración inválida del ticket " + tt.getName() + " (Maximo de Compra)");

            int qtyFinal = qty + qtyPrev;

            if (qtyFinal > qLimit)
                throw new BusinessException("Superaste el limite de compra de " + tt.getName());
        }
         return new TicketValidationResult(ticketTypeMap, itemsQuantity);
    }

    /**
     * Valida que los usuarios asignados existan y
     * coincidan con la configuración del ticket.
     */
    private void validateAssignedUsers(
            List<ItemsDTO> items,
            Map<Long, TicketType> ticketTypeMap
    ) {
        List<String> users = items.stream()
                .map(ItemsDTO::getAssignedUsersId)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .toList();

        if (users.size() > 1) {
            List<String> missing = userRepository.missingUserIds(users);
            if (!missing.isEmpty()) {
                throw new BusinessException("Usuarios no encontrados: " + missing);
            }
        }

        for (var item : items) {
            TicketType ticketType = ticketTypeMap.get(item.getTicketTypeId());
            int assigned = item.getAssignedUsersId() == null ? 0 : item.getAssignedUsersId().size();
            if (assigned != ticketType.getSeatsPerGroup()) {
                throw new BusinessException(
                        "El ticket " + ticketType.getName() + " requiere asignación válida"
                );
            }
        }
    }

    /**
     * Ejecuta el pago mediante el procesador configurado.
     */
    private TransactionEntity processPayment(String ip, CheckoutDTO dto, OrderEntity order) {
        var processCheckout = processCheckoutFactory.processCheckout("processor");

        CheckoutProcessor processor = new CheckoutProcessor(
                dto.payment().method(),
                dto.payment().processorId(),
                ip,
                dto.payment().token()
        );

        processor.setOrder(order);

        var response = processCheckout.process(processor);
        TransactionEntity tx = response.getTransaction();

        if (TxStatusEnum.REJECTED.getCode().equals(tx.getStatus())) {
            throw new BusinessException("Pago rechazado");
        }

        return tx;
    }

    /**
     * Actualiza el estado de la orden luego del pago.
     */
    private void updateOrderAfterPayment(OrderEntity order, TransactionEntity tx) {
        OrderEntity entity = new OrderEntity();
        entity.setId(order.getId());
        entity.setEventId(order.getEventId());
        entity.setPaymentReference(tx.getId().toString());
        entity.setUpdatedDatetime(LocalDateTime.now());
        entity.setUpdatedTimestamp(EpochUtils.inSeconds());

        if (TxStatusEnum.PENDING.getCode().equals(tx.getStatus())) {
            entity.setStatus(OrderStatusEnum.WAITING_PROCESSOR.getCode());
        } else if (TxStatusEnum.COMPLETED.getCode().equals(tx.getStatus())) {
            entity.setStatus(OrderStatusEnum.PAID.getCode());
        }

        orderRepository.updateStatus(entity);
    }

    /**
     * Marca la orden como completada.
     */
    private void markOrderCompleted(Long orderId, Long eventId) {
        OrderEntity entity = new OrderEntity();
        entity.setId(orderId);
        entity.setEventId(eventId);
        entity.setStatus(OrderStatusEnum.COMPLETED.getCode());
        entity.setUpdatedDatetime(LocalDateTime.now());
        entity.setUpdatedTimestamp(EpochUtils.inSeconds());
        orderRepository.updateStatus(entity);
    }

    /**
     * Maneja errores del checkout y libera stock.
     */
    private void handleCheckoutFailure(
            OrderEntity order,
            String paymentRef,
            boolean ticketsGenerated,
            boolean paid,
            Map<Long, TicketType> ticketTypeMap
    ) {
        try {
            OrderEntity entity = new OrderEntity();
            entity.setId(order.getId());
            entity.setPaymentReference(paymentRef);
            entity.setUpdatedDatetime(LocalDateTime.now());
            entity.setUpdatedTimestamp(EpochUtils.inSeconds());
            entity.setEventId(order.getEventId());

            if (!ticketsGenerated) {
                entity.setStatus(OrderStatusEnum.FULFILLMENT_FAILED.getCode());
            } else if (!paid) {
                entity.setStatus(OrderStatusEnum.PAYMENT_FAILED.getCode());
            } else {
                entity.setStatus(OrderStatusEnum.ERROR.getCode());
            }

            orderRepository.updateStatus(entity);

        } catch (Exception e) {
            log.error("[checkout] Error marcando orden fallida orderId={}", order.getId(), e);
        }

        releaseStock(order, ticketTypeMap);
    }

    /**
     * Libera el stock reservado por la orden.
     */
    private void releaseStock(OrderEntity order, Map<Long, TicketType> ticketTypeMap) {
        for (var item : order.getItems()) {
            int updated = ticketTypeRepository.releaseStock(
                    item.getProductId(),
                    item.getQuantity()
            );
            if (updated == 0) {
                log.error(
                        "[checkout] No se pudo liberar stock ticketType={}",
                        ticketTypeMap.get(item.getProductId()).getName()
                );
            }
        }
    }
}
