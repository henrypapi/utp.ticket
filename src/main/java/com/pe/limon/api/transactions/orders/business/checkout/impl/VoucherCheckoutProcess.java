package com.pe.limon.api.transactions.orders.business.checkout.impl;

import com.pe.limon.api.core.utils.exception.BusinessException;
import com.pe.limon.api.core.utils.file.FileUtil;
import com.pe.limon.api.transactions.orders.business.checkout.models.*;
import com.pe.limon.api.transactions.orders.business.checkout.IProcessCheckout;

import com.pe.limon.api.transactions.wallet.repository.entity.VoucherEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component("voucher")
@Slf4j
public class VoucherCheckoutProcess implements IProcessCheckout {
    @Value("${application.images.directory.voucher}")
    private String imageUploadPath;
    @Override
    public PaymentMode mode() {
        return PaymentMode.VOUCHER;
    }

    @Override
    public CheckoutResponse process(CheckoutContext context) {
        CheckoutVoucher checkoutVoucher = (CheckoutVoucher) context;

        var file = checkoutVoucher.getVoucher();
        var voucher = new VoucherEntity();
        if (file != null) {
            log.debug("Imagen enviada, guardando");
            String fileName = "voucher_" +
                    System.currentTimeMillis() +
                    "." +
                    Objects.requireNonNull(file.getOriginalFilename())
                            .substring(file.getOriginalFilename().lastIndexOf('.') + 1);

            try {
                FileUtil.saveFile(file, fileName, imageUploadPath);
                voucher.setFileName(fileName);
            } catch (IOException e) {
                throw new BusinessException("No se pudo guardar la imagen del evento" + e.getMessage());
            }
        }
        return new CheckoutResponse(null,voucher);
    }

}