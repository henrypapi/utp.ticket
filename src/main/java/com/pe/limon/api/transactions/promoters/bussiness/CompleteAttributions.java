package com.pe.limon.api.transactions.promoters.bussiness;

import com.pe.limon.api.core.utils.conversor.JsonUtils;
import com.pe.limon.api.core.utils.exception.InternalServerException;
import com.pe.limon.api.transactions.tickets.repository.entity.TicketType;
import com.pe.limon.api.transactions.orders.repository.entity.OrderEntity;
import com.pe.limon.api.transactions.orders.repository.entity.OrderItemEntity;
import com.pe.limon.api.transactions.promoters.bussiness.entity.PromoterEntity;
import com.pe.limon.api.transactions.promoters.repository.PromoterRepository;
import com.pe.limon.api.transactions.promoters.repository.entity.SaleDetail;
import com.pe.limon.api.transactions.promoters.repository.entity.TotalSaleDetails;
import com.pe.limon.api.transactions.tickets.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
@RequiredArgsConstructor
public class CompleteAttributions {
    private final PromoterRepository promoterRepository;

    public void execute(PromoterEntity promoter, Map<Long, TicketType> ticketTypeMap, Map<Long, OrderItemEntity>  mapOrderItems, OrderEntity order) {
        log.info("[execute] CompleteAttributions Starting {}", promoter);

        TotalSaleDetails details;
        if (promoter.getTotalSalesDetails()!=null
                && !promoter.getTotalSalesDetails().isEmpty() && !promoter.getTotalSalesDetails().isBlank()) {
            details = JsonUtils.convertToObject(promoter.getTotalSalesDetails(), TotalSaleDetails.class);

            if(details == null) throw new InternalServerException("Error interno");
        }else{
            log.info("[completeAttributions] SaleDetails is empty {}", promoter);
            details = new TotalSaleDetails();
        }

        if (details.getTickets()==null){
            log.info("[completeAttributions] SaleDetails.getTickets is empty {}", promoter);
            var ticketsDetails = new ArrayList<SaleDetail>();
            details.setTickets(ticketsDetails);
        }

        Map<Long, Integer> idToIndex = new HashMap<>();

        for (int i = 0; i < details.getTickets().size(); i++) {
            var t = details.getTickets().get(i);
            if (t.getTicketTypeId() != null) {
                idToIndex.put(t.getTicketTypeId(), i);
            }
        }

        log.info("[completeAttributions] Init main Loop {}",promoter);

        for (Long key : mapOrderItems.keySet()) {
            log.info("[completeAttributions] item {}", mapOrderItems.get(key));
            var orderItem = mapOrderItems.get(key);
            var quantity = orderItem.getQuantity();
            var subTotal = orderItem.getSubtotal();
            SaleDetail ticketDetail;
            if (idToIndex.containsKey(key)){
                ticketDetail = details.getTickets().get(idToIndex.get(key));
                quantity += ticketDetail.getSalesCount();
                subTotal = BigDecimal.valueOf(ticketDetail.getSalesAmount()).add(subTotal);
            }else{
                ticketDetail = new SaleDetail();
                ticketDetail.setTicketTypeId(key);
                var tt = ticketTypeMap.get(key);
                ticketDetail.setTicketTypeName(tt != null ? tt.getName() : "—");
                details.getTickets().add(ticketDetail);
                idToIndex.put(key,details.getTickets().size()-1);
            }

            ticketDetail.setSalesCount(quantity);
            ticketDetail.setSalesAmount(subTotal.floatValue());
        }

        promoter.setTotalSalesDetails(JsonUtils.convertToJsonString(details));
        var newTotalAmount = promoter.getTotalSalesAmount().add(order.getTotalAmount());
        var newTotalCount = promoter.getTotalSalesCount() + order.getTotalQuantity();

        promoter.setTotalSalesAmount(newTotalAmount);
        promoter.setTotalSalesCount(newTotalCount);
        int upd = promoterRepository.updateTotalSales(promoter);
        log.info("[execute] promoter sales updated {} ", upd);
    }
}
