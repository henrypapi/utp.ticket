package com.pe.limon.api.transactions.wallet.business;

import com.google.zxing.WriterException;
import com.mercadopago.resources.payment.Payment;
import com.pe.limon.api.core.utils.conversor.JsonUtils;
import com.pe.limon.api.core.utils.exception.BusinessException;
import com.pe.limon.api.core.utils.conversor.EpochUtils;
import com.pe.limon.api.core.utils.exception.NotFoundException;
import com.pe.limon.api.gateway.admin.events.dto.PageResult;
import com.pe.limon.api.gateway.admin.wallet.dto.TransactionDto;
import com.pe.limon.api.gateway.admin.wallet.dto.TransactionType;
import com.pe.limon.api.gateway.admin.wallet.dto.TransactionOperation;
import com.pe.limon.api.transactions.events.repository.EventRepository;
import com.pe.limon.api.transactions.tickets.repository.entity.TicketType;
import com.pe.limon.api.transactions.orders.business.orders.CheckoutTransactionManager;
import com.pe.limon.api.transactions.orders.business.orders.model.CreateOrderOut;
import com.pe.limon.api.transactions.orders.business.orders.model.OrderStatusEnum;
import com.pe.limon.api.transactions.orders.repository.OrderAttributionRepository;
import com.pe.limon.api.transactions.orders.repository.OrderItemRepository;
import com.pe.limon.api.transactions.orders.repository.OrderRepository;
import com.pe.limon.api.transactions.orders.repository.entity.OrderEntity;
import com.pe.limon.api.transactions.orders.repository.entity.OrderItemEntity;
import com.pe.limon.api.transactions.payment.dto.PaymentResult;
import com.pe.limon.api.transactions.payment.processors.impl.mercadopago.UtilsPayment;
import com.pe.limon.api.transactions.tickets.repository.TicketRepository;
import com.pe.limon.api.transactions.tickets.repository.TicketTypeRepository;
import com.pe.limon.api.transactions.wallet.repository.entity.TransactionEntity;
import com.pe.limon.api.transactions.wallet.repository.TransactionRepository;
import com.pe.limon.api.transactions.wallet.repository.entity.WalletEntity;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    private final TransactionRepository transactionRepository;
    private final CheckoutTransactionManager createOrderAndAttributionsTx;
    private final UtilsPayment utilsPayment;
    private final OrderRepository orderRepository;
    private final OrderAttributionRepository orderAttributionRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final OrderItemRepository orderItemRepository;
    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;

    public PageResult<TransactionDto> getTransactions(String userId, Integer size, Integer page) {

        PageResult<TransactionEntity> entityPageResult = transactionRepository.findByDays(
                userId, page, size);
        List<TransactionDto> dtoList = entityPageResult.getContent().stream().map(entity -> {
            TransactionDto dto = new TransactionDto();
            dto.setId(entity.getId());
            dto.setType(TransactionType.valueOf(entity.getType()));
            dto.setOperation(TransactionOperation.valueOf(entity.getOperation()));
            dto.setMethodId(entity.getMethodId());
            dto.setAmount(entity.getAmount());
            dto.setTimestamp(entity.getRegisteredTimestamp());
            return dto;
        }).toList();

        return new PageResult<>(dtoList, page, size, entityPageResult.getTotalElements());
    }

    @Transactional
    public void processPayment(String paymentId, String processorId) {
        Long paymentIdNum = Long.parseLong(paymentId);
        log.info("[processPayment] paymentIdNum: {}", paymentIdNum);

        try {
            Payment payment = utilsPayment.getPaymentById(paymentIdNum);
            log.debug("[processPayment] payment {}", payment.toString());
            log.info("[processPayment] payment status{}", payment.getStatus());
            log.info("[processPayment] payment status detail{}", payment.getStatusDetail());
            log.info("[processPayment] payment ext ref{}", payment.getExternalReference());

            String status = payment.getStatus();               // e.g. "approved"
            String statusDetail = payment.getStatusDetail();   // e.g. "accredited"
            String externalRef = payment.getExternalReference(); // para mapear a tu orderId
            Long orderId = Long.parseLong(externalRef);
            var order = orderRepository.findById(orderId);

            if (order == null){
                log.error("[processPayment] No se encontro la orden indiicada {}", orderId);
                throw new BusinessException("No se encontro la orden indicada "+orderId);
            }

            log.info("[processPayment] order {}", order.toString());

            if(order.getStatus().equals(OrderStatusEnum.COMPLETED.getCode())) {
                log.info("[processPayment] Order completada {}", orderId);
                return;
            }

            if(order.getStatus().equals(OrderStatusEnum.PAYMENT_FAILED.getCode()) || order.getStatus().equals(OrderStatusEnum.ERROR.getCode())) {
                log.error("[processPayment] Order con errores {}, subsanar para completar pago", orderId);
                return;
            }

            String statusTx = "";
            String statusOrder = "";
            switch (status) {
                case "rejected" -> {
                     statusTx = TxStatusEnum.REJECTED.getCode();
                     statusOrder = OrderStatusEnum.PAYMENT_FAILED.getCode();
                    releaseOrderStock(order.getItems());
                }
                case "pending","in_process","authorized" -> {
                    statusTx = TxStatusEnum.PENDING.getCode();
                    statusOrder = OrderStatusEnum.WAITING_PROCESSOR.getCode();
                }
                case "approved" -> {
                    if (statusDetail.equals("accredited")) {
                        statusTx = TxStatusEnum.COMPLETED.getCode();
                        statusOrder = OrderStatusEnum.COMPLETED.getCode();
                    } else {
                        log.error("[processPayment] Estado no valido {}", statusDetail);
                        return;
                    }
                }
            }

            // external_reference es lo típico para “enganchar” tu orden interna
            TransactionEntity transaction = new TransactionEntity();
            transaction.setStatus(statusTx);
            transaction.setProcessorStatus(status);
            transaction.setUpdatedDatetime(LocalDateTime.now());
            transaction.setUpdatedTimestamp(EpochUtils.inSeconds());
            transaction.setMetadata(JsonUtils.convertToJsonString(payment));
            transaction.setProcessorReference(paymentId);
            transaction.setType("P");
            transaction.setProcessorId(processorId);
            transaction.setOperation(OperationEnum.CREDIT.getCode());
            var rowAffected = transactionRepository.updateStatus(transaction);

            if (rowAffected < 1) throw new BusinessException("Error al actualizar el estado de la transacción");

            if (!statusTx.equals(TxStatusEnum.COMPLETED.getCode())){
                var event = eventRepository.findById(order.getEventId());
                if (event==null) throw new NotFoundException("No se pudo encontrar el evento asosciaod a la orden: "+ order.getId());
                completeOrderAfterPaymentApproved(order,transaction, event.getQrEnabled());
            }

            order.setStatus(statusOrder);
            order.setUpdatedDatetime(LocalDateTime.now());
            order.setUpdatedTimestamp(EpochUtils.inSeconds());
            order.setId(orderId);
            rowAffected = orderRepository.updateStatus(order);
            if (rowAffected < 1) throw new BusinessException("Error al actualizar el estado de la orden");

        } catch (Exception e) {
            log.error("Error to process webhook ",e);
            throw new RuntimeException("Error consultando payment en Mercado Pago: " + paymentId, e);
        }
    }

    @Transactional
    public TransactionEntity savePurchaseAndCommission(
            WalletEntity wallet,
            String processorId,
            String methodId,
            OrderEntity order,
            PaymentResult paymentResult
    ) {

        TransactionEntity purchase = new TransactionEntity();
        purchase.setProcessorId(processorId);
        purchase.setCurrency("PEN");
        purchase.setAmount(order.getNetAmount());
        purchase.setRegisteredDatetime(LocalDateTime.now());
        purchase.setRegisteredTimestamp(EpochUtils.inSeconds());
        purchase.setOperation("C");
        purchase.setType("P");
        purchase.setWalletId(wallet.getId());
        purchase.setStatus(paymentResult.getTxStatus());
        purchase.setProcessorStatus(paymentResult.getProcessorStatus());
        purchase.setProcessorReference(paymentResult.getProcessorRef());
        purchase.setMetadata(JsonUtils.convertToJsonString(paymentResult.getData()));
        purchase.setMethodId(methodId);
        long txId = transactionRepository.insert(purchase);
        purchase.setId(txId);
        return purchase;
    }

    public void completeOrderAfterPaymentApproved(OrderEntity order, TransactionEntity transaction, boolean qrEnabled) throws IOException, WriterException {
        long orderId = order.getId();
        log.info("[completeOrderAfterPaymentApproved] Starting");
        var orderAttribution = orderAttributionRepository.findById(orderId);
        Map<Long, TicketType> ticketTypeMap = Map.of();
        Map<Long,OrderItemEntity>  mapOrderItems = new HashMap<>();
        var ticketIds = ticketRepository.findIdsByOrderId(orderId);
        var orderItems = orderItemRepository.findByOrderId(orderId);
        order.setItems(orderItems);
        if (orderAttribution != null){
            log.info("[completeOrderAfterPaymentApproved] completeAttribution {}", orderId);
            order.setOrderAttribution(orderAttribution);
        }

        var listTicketsTypes = ticketTypeRepository.findByAndEventIdAndIdsEnableTimestampAndActive(
                order.getEventId(),
                true,
                EpochUtils.inSeconds(),
                orderItems.stream().map(OrderItemEntity::getProductId).toList());

        ticketTypeMap = listTicketsTypes.stream()
                .collect(Collectors.toMap(
                        TicketType::getId,   // key
                        tt -> tt             // value
                ));

        for (var orderItem : orderItems){
            mapOrderItems.put(orderItem.getId(),orderItem);
        }
        createOrderAndAttributionsTx.completeCheckout(
                order.getUserId(),
                order.getEventId(),
                qrEnabled,
                transaction,
                ticketIds,
                ticketTypeMap,
                new CreateOrderOut(mapOrderItems, order)
        );
    }

    public void releaseOrderStock(List<OrderItemEntity> orderItems){
        Long productId = 0L;
        try{
            for (var orderItem:orderItems){
                productId = orderItem.getProductId();
                var upd = ticketTypeRepository.releaseStock(orderItem.getProductId(), orderItem.getQuantity());
                if (upd == 0) throw new BusinessException("Stock no encontrado para ticketTypeId: " + orderItem.getProductId());
            }
        }catch (Exception ex) {
            log.error("[processPayment] Failed to release stock. ticketTypeId={}", productId, ex);
        }
    }
}
