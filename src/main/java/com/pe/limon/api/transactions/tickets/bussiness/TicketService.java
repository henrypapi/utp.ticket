package com.pe.limon.api.transactions.tickets.bussiness;

import com.google.zxing.WriterException;
import com.pe.limon.api.core.utils.conversor.EpochUtils;
import com.pe.limon.api.core.utils.enums.StatusEnum;
import com.pe.limon.api.gateway.client.checkout.dto.ItemsDTO;
import com.pe.limon.api.transactions.orders.repository.entity.OrderEntity;
import com.pe.limon.api.transactions.tickets.repository.ControlAccessRepository;
import com.pe.limon.api.transactions.tickets.repository.entity.AccessPassEntity;
import com.pe.limon.api.transactions.tickets.repository.entity.TicketType;
import com.pe.limon.api.transactions.orders.repository.entity.OrderItemEntity;
import com.pe.limon.api.transactions.tickets.repository.TicketRepository;
import com.pe.limon.api.transactions.tickets.repository.entity.TicketEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ControlAccessRepository controlAccessRepository;
    private final QrService qrService;

    public List<OrderEntity> getByOwnerUserId(String userId){
        return ticketRepository.findByAssignedUserId(userId);
    }

    public List<AccessPassEntity> getByOwnerUserIdAndOrderId(String userId, Long orderId){
        return controlAccessRepository.findByAssignedUserIdAndTicketId(userId, orderId);
    }

    public List<Long> generateTicket(String userId, boolean noReqPayment,List<ItemsDTO> items, Map<Long, TicketType> ticketTypeMap, List<OrderItemEntity> orderItems) throws WriterException, IOException {
        int count=1;
        var listTicketIds = new ArrayList<Long>();
        Map<Long, OrderItemEntity> itemByTicketType =
            orderItems.stream()
                .collect(Collectors.toMap(
                    OrderItemEntity::getProductId,
                    Function.identity()
                ));

        for (var itemDTO : items) {
            var ticketType = ticketTypeMap.get(itemDTO.getTicketTypeId());

            TicketEntity ticket = new TicketEntity();
            ticket.setLabelName(ticketType.getName()+"-"+count);
            ticket.setTicketTypeId(itemDTO.getTicketTypeId());
            ticket.setOrderItemId(itemByTicketType.get(itemDTO.getTicketTypeId()).getId());
            ticket.setTicketType(ticketType);
            ticket.setRegisteredBy("system");
            ticket.setRegisteredTimestamp(EpochUtils.inSeconds());
            ticket.setRegisteredDatetime(LocalDateTime.now());
            var ticketId = ticketRepository.insert(ticket);

            List<AccessPassEntity> accessPasses = new ArrayList<>();
            for (var assignedUserId : itemDTO.getAssignedUsersId()) {
                AccessPassEntity accessPass = new AccessPassEntity();
                accessPass.setTicketId(ticketId);
                accessPass.setAssignedBy(userId);
                accessPass.setAssignedTimestamp(EpochUtils.inSeconds());
                accessPass.setAssignedDatetime(LocalDateTime.now());
                accessPass.setAssignedUserId(userId);

                if (noReqPayment){
                    accessPass = qrService.getWithContent(accessPass);
                }else{
                    accessPass.setStatus(StatusEnum.INACTIVE.getCode());
                    accessPass.setCode(null);
                }
                accessPasses.add(accessPass);
            }

            qrService.registerAccessPass(accessPasses);
            count++;
            listTicketIds.add(ticketId);
        }
        return listTicketIds;
    }
}
