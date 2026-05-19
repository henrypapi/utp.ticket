package com.pe.limon.api.transactions.promoters.repository.entity;

import lombok.Data;

@Data
public class SaleDetail {
    private Long ticketTypeId;
    private String ticketTypeName;
    private int salesCount;
    private float salesAmount;
}
