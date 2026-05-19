package com.pe.limon.api.transactions.orders.business.checkout.models;

import com.pe.limon.api.transactions.tickets.repository.entity.TicketType;

import java.util.Map;

public record TicketValidationResult(Map<Long, TicketType> ticketTypeMap,
                              Map<Long, Integer> itemsQuantity) {}