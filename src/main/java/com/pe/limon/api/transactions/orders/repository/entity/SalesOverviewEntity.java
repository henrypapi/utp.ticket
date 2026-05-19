package com.pe.limon.api.transactions.orders.repository.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalesOverviewEntity {
    private BigDecimal revenue;
    private Integer ticketsSold;
    private Integer totalTickets;
    private Double occupancyPercentage;
}