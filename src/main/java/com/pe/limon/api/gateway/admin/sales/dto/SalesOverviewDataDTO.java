package com.pe.limon.api.gateway.admin.sales.dto;


public record SalesOverviewDataDTO(
    double revenue,
    int ticketsSold,
    int totalTickets,
    double occupancyPercentage
) {
}