package com.pe.limon.api.transactions.promoters.repository.entity;


import lombok.Data;

import java.util.List;

@Data
public class TotalSaleDetails {
    private List<SaleDetail> tickets;
}
