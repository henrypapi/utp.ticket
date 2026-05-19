package com.pe.limon.api.gateway.admin.sales.dto;

import java.util.List;


public record SalesOverviewDTO(SalesOverviewDataDTO overview, List<TicketTypeSalesDTO> byTicketType) {
}