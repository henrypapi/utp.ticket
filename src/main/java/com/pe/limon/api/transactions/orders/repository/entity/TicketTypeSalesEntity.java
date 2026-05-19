package com.pe.limon.api.transactions.orders.repository.entity;

import lombok.Data;

@Data
public class TicketTypeSalesEntity {
    private long id;
    private String name;
    private int sold;
    private int stock;
}