package com.pe.limon.api.transactions.orders.business.orders;

import com.google.zxing.WriterException;
import com.pe.limon.api.core.utils.conversor.EpochUtils;
import com.pe.limon.api.core.utils.exception.BusinessException;
import com.pe.limon.api.core.utils.exception.NotFoundException;
import com.pe.limon.api.core.utils.file.FileUtil;
import com.pe.limon.api.gateway.admin.events.dto.PageResult;
import com.pe.limon.api.gateway.admin.sales.dto.*;
import com.pe.limon.api.transactions.authz.repository.PersonalInfoRepository;
import com.pe.limon.api.transactions.events.business.PaymentModeEnum;
import com.pe.limon.api.transactions.events.repository.EventRepository;
import com.pe.limon.api.transactions.orders.business.orders.model.OrderStatusEnum;
import com.pe.limon.api.transactions.orders.repository.OrderItemRepository;
import com.pe.limon.api.transactions.orders.repository.OrderRepository;
import com.pe.limon.api.transactions.orders.repository.SalesRepository;
import com.pe.limon.api.transactions.orders.repository.entity.OrderEntity;
import com.pe.limon.api.transactions.tickets.repository.TicketRepository;
import com.pe.limon.api.transactions.tickets.repository.TicketTypeRepository;
import com.pe.limon.api.transactions.tickets.repository.entity.AccessPassEntity;
import com.pe.limon.api.transactions.wallet.business.TransactionService;
import com.pe.limon.api.transactions.wallet.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final SalesRepository salesRepository;
    private final PersonalInfoRepository personalInfoRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final EventRepository eventRepository;
    private final CheckoutTransactionManager checkoutTransactionManager;
    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;
    private final TicketRepository ticketRepository;
    @Value("${application.images.directory.voucher}")
    private String VOUCHER_DIRECTORY;
    public PageResult<SaleReponseDTO> getSalesByEventId(Long eventId, String orderStatus,int page, int size) {
        PageResult<OrderEntity> result = orderRepository.getSalesByEventId(eventId,orderStatus, page, size);
        List<SaleReponseDTO> content = result.getContent().stream()
            .map(entity -> new SaleReponseDTO(
                entity.getId(),
                entity.getUserId(),
                entity.getTotalAmount().doubleValue(),
                entity.getStatus(),
                entity.getPaymentReference(),
                entity.getRegisteredTimestamp()
            ))
            .collect(Collectors.toList());
        return new PageResult<>(content, result.getPage(), result.getSize(), result.getTotalElements());
    }

    public SalesOverviewDTO getSalesOverviewByEventId(Long eventId) {
        var overviewEntity = salesRepository.getSalesOverviewByEventId(eventId,OrderStatusEnum.COMPLETED.getCode());
        var ticketTypeSalesEntities = salesRepository.getTicketTypeSalesByEventId(eventId);

        SalesOverviewDataDTO overview = new SalesOverviewDataDTO(
            overviewEntity.getRevenue().doubleValue(),
            overviewEntity.getTicketsSold(),
            overviewEntity.getTotalTickets(),
            overviewEntity.getOccupancyPercentage()
        );

        List<TicketTypeSalesDTO> byTicketType = ticketTypeSalesEntities.stream()
            .map(entity -> new TicketTypeSalesDTO(
                entity.getId(),
                entity.getName(),
                entity.getSold(),
                entity.getStock()
            ))
            .collect(Collectors.toList());

        return new SalesOverviewDTO(overview, byTicketType);
    }

    public OrderDetailsDTO getOrderDetailsById(Long orderId, String userId) {
        var orderDetailsEntity = orderRepository.findById(orderId);
        if (userId != null && !orderDetailsEntity.getUserId().equals(userId)) throw new BusinessException("No puedes obtener informacion de esta orden.");

        return getOrderDetailsDTO(orderDetailsEntity);
    }

    public OrderDetailsDTO getOrderDetailsById(Long orderId, Long eventId) {
        var orderDetailsEntity = orderRepository.findByIdAndEventId(eventId,orderId);
        return getOrderDetailsDTO(orderDetailsEntity);
    }

    private OrderDetailsDTO getOrderDetailsDTO(OrderEntity orderDetailsEntity) {
        var orderItemEntities = orderItemRepository.findByOrderId(orderDetailsEntity.getId());

        var buyerEntity = personalInfoRepository.findByUserId(orderDetailsEntity.getUserId());

        List<OrderItemDTO> items = orderItemEntities.stream()
                .map(itemEntity -> {
                    var ticketType = ticketTypeRepository.findById(itemEntity.getProductId());
                    var ticketTypeDTO = new TicketTypeDTO(itemEntity.getProductId(), ticketType.get().getName());
                    var ticketDTO = new TicketDTO(ticketTypeDTO);
                    return new OrderItemDTO(
                            itemEntity.getId(),
                            itemEntity.getQuantity(),
                            itemEntity.getUnitPrice(),
                            itemEntity.getSubtotal(),
                            ticketDTO
                    );
                })
                .collect(Collectors.toList());

        var buyerDTO = new BuyerDTO(
                buyerEntity.getFirstName(),
                buyerEntity.getLastName(),
                buyerEntity.getEmail(),
                buyerEntity.getPhoneNumber()
        );

        return new OrderDetailsDTO(
                orderDetailsEntity.getId(),
                orderDetailsEntity.getUpdatedTimestamp(),
                orderDetailsEntity.getTotalQuantity(),
                orderDetailsEntity.getTotalAmount(),
                orderDetailsEntity.getNetAmount(),
                orderDetailsEntity.getCommissionAmount(),
                orderDetailsEntity.getPaymentReference(),
                orderDetailsEntity.getStatus(),
                items,
                buyerDTO
        );
    }

    public FileSystemResource getFile(Long eventId, Long orderId) {
        var order = orderRepository.findByIdAndEventId(eventId, orderId);

        if (order == null
        ) throw new BusinessException("Order not found.");

        File file = FileUtil.getFileFromDirectory(VOUCHER_DIRECTORY, order.getPaymentReference());

        if (!file.exists() || !file.isFile()) throw new BusinessException("File not found.");
        return new FileSystemResource(file);
    }


    public FileSystemResource getClientFile( Long orderId, String userId) {
        var order = orderRepository.findByIdAndUserId(orderId, userId);

        if (order == null) throw new BusinessException("Order not found.");

        File file = FileUtil.getFileFromDirectory(VOUCHER_DIRECTORY, order.getPaymentReference());

        if (!file.exists() || !file.isFile()) throw new BusinessException("File not found.");
        return new FileSystemResource(file);
    }

    @Transactional
    public void updateStatus(Long eventId, Long orderId, String action, String userId) throws IOException, WriterException {

        var event = eventRepository.findById(eventId);
        if (event == null) throw new NotFoundException("Evento no encontrado.");

        if (!PaymentModeEnum.VOUCHER.getCode().equals(event.getPaymentMode()))
            throw new BusinessException("No puedes actualizar esta orden.");

        var order = orderRepository.findById(orderId);

        if (!order.getStatus().equals(OrderStatusEnum.WAITING_PROCESSOR.getCode()))
            throw new NotFoundException("No puedes actualizar esta orden.");

        String status = "";
        switch (action) {
            case "approve" ->{
                status = OrderStatusEnum.COMPLETED.getCode();
                transactionService.completeOrderAfterPaymentApproved(order, null, event.getQrEnabled());
            }
            case "reject" -> {
                var orderItems = orderItemRepository.findByOrderId(orderId);
                status = OrderStatusEnum.REJECTED.getCode();
                transactionService.releaseOrderStock(orderItems);
            }
            default -> throw new BusinessException("Accion no permitida");
        }

        order.setStatus(status);
        order.setEventId(eventId);
        order.setUpdatedDatetime(LocalDateTime.now());
        order.setUpdatedTimestamp(EpochUtils.inSeconds());
        order.setId(orderId);
        order.setUpdatedBy(userId);
        var rowAffected = orderRepository.updateStatus(order);
        if (rowAffected < 1) throw new BusinessException("Error al actualizar el estado de la orden");

    }
}