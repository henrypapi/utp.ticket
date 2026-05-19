package com.pe.limon.api.transactions.orders.business.orders;

import com.google.zxing.WriterException;
import com.pe.limon.api.core.utils.conversor.EpochUtils;
import com.pe.limon.api.core.utils.exception.BusinessException;
import com.pe.limon.api.gateway.client.checkout.dto.AttributionDTO;
import com.pe.limon.api.gateway.client.checkout.dto.ItemsDTO;
import com.pe.limon.api.transactions.tickets.repository.entity.TicketType;
import com.pe.limon.api.transactions.orders.business.orders.model.CreateOrderOut;
import com.pe.limon.api.transactions.orders.repository.OrderAttributionRepository;
import com.pe.limon.api.transactions.orders.repository.entity.OrderAttributionEntity;
import com.pe.limon.api.transactions.orders.repository.entity.OrderItemEntity;
import com.pe.limon.api.transactions.promoters.bussiness.CompleteAttributions;
import com.pe.limon.api.transactions.promoters.repository.PromoterRepository;
import com.pe.limon.api.transactions.tickets.bussiness.QrService;
import com.pe.limon.api.transactions.tickets.bussiness.TicketService;
import com.pe.limon.api.transactions.tickets.repository.TicketTypeRepository;
import com.pe.limon.api.transactions.wallet.business.WalletService;
import com.pe.limon.api.transactions.wallet.repository.entity.TransactionEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutTransactionManager {
    private final CreateOrder createOrder;
    private final CompleteAttributions completeAttributions;
    private final QrService qrService;
    private final OrderAttributionRepository orderAttributionRepository;
    private final TicketService ticketService;
    private final PromoterRepository promoterRepository;
    private final WalletService walletService;
    private final TicketTypeRepository ticketTypeRepository;

    @Transactional(rollbackFor = Exception.class)
    public CreateOrderOut createOrder(String userId,
                              Map<Long, TicketType> ticketTypeMap,
                              Long eventId,
                              Map<Long, Integer> itemsQuantity,
                              AttributionDTO attribution,
                              String paymentReference,
                              boolean processorPayment) {
        log.info("[execute] Starting {} - {}", userId, eventId);

        var orderCreated = createOrder.execute(userId, ticketTypeMap, eventId, paymentReference, itemsQuantity ,processorPayment);
        if (attribution == null || attribution.promoterCode() == null
                || attribution.promoterCode().isEmpty()
                || attribution.promoterCode().isBlank()) return orderCreated;

        log.info("[execute] Starting completeAttributions {}", attribution);
        var promoter = promoterRepository.findByEventIdAndCode(eventId, attribution.promoterCode(),null);
        if (promoter == null) return orderCreated;
        log.info("[execute] attributions not empty {}", attribution);
        if (!promoter.isActive()) return orderCreated;
        log.info("[execute] attributions is active {}", attribution);

        var totalUses = promoter.getTotalUses() == null ? 0 : promoter.getTotalUses();
        var maxUses = promoter.getMaxUses() == null ? 0 : promoter.getMaxUses();

        if ((totalUses+1)>maxUses){
            log.info("[execute] supero max uses {}", attribution);
            return orderCreated;
        }

        OrderAttributionEntity orderAttribution = new OrderAttributionEntity();
        orderAttribution.setOrderId(orderCreated.getOrder().getId());
        orderAttribution.setPromoterId(promoter.getId());
        orderAttribution.setPromoter(promoter);
        orderAttribution.setRegisteredTimestamp(EpochUtils.inSeconds());
        orderAttribution.setRegisteredDatetime(LocalDateTime.now());
        var oAttIsOk = orderAttributionRepository.insert(orderAttribution);

        if (!oAttIsOk) throw new BusinessException("Error al atribuir la orden al promotor");

        orderCreated.getOrder().setOrderAttribution(orderAttribution);
        log.info("[execute] Ending ok {} - {}", userId, eventId);
        return orderCreated;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Long> generateTickets(String userId, boolean noReqPayment,List<ItemsDTO> itemsDTOS, Map<Long, TicketType> ticketTypeMap, List<OrderItemEntity> items) throws IOException, WriterException {
        log.info("[generateTicket] Starting {}", userId);

        List<Long> ticketIds = ticketService.generateTicket(userId,noReqPayment,itemsDTOS,ticketTypeMap,items);
        log.info("[generateTicket] Generate Tickets for user {} ", userId);
        return ticketIds;
    }

    @Transactional(rollbackFor = Exception.class)
    public void completeCheckout(String userId,
                                 long eventId,
                                 boolean generateQR,
                                 TransactionEntity transaction,
                                 List<Long> ticketIds,
                                 Map<Long, TicketType> ticketTypeMap,
                                 CreateOrderOut orderCreated) throws IOException, WriterException {
        log.info("[afterPaymentCompleted] Starting {}", userId);
        log.info("[afterPaymentCompleted] ticketIds {}",ticketIds);
        log.info("[afterPaymentCompleted] generateQR {}",generateQR);

        for (Long ticketId : ticketIds) {
            qrService.generateAccessCode(ticketId,generateQR);
        }

        log.info("[completeCheckout] CompleteAttributions Starting {}", orderCreated);
        if (orderCreated.getOrder().getOrderAttribution() != null)
            completeAttributions.execute(orderCreated.getOrder().getOrderAttribution().getPromoter(), ticketTypeMap, orderCreated.getMapOrderItems(), orderCreated.getOrder());

        if (transaction != null)
            walletService.refreshBalance(eventId,List.of(transaction));

        for (var orderItem : orderCreated.getOrder().getItems()) {
            log.info("[completeCheckout]orderItem {}", orderItem);

            int updReserved = ticketTypeRepository.confirmStock(orderItem.getProductId(), orderItem.getQuantity());
            if (updReserved == 0) throw new BusinessException("Stock no encontrado para Ticket: " + ticketTypeMap.get(orderItem.getProductId()).getName());
        }

        log.info("[afterPaymentCompleted] Generate Tickets for user {} ", userId);
    }
}
