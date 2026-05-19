package com.pe.limon.api.transactions.orders.business.checkout.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.web.multipart.MultipartFile;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class CheckoutVoucher extends CheckoutContext{
    private MultipartFile voucher;
}
