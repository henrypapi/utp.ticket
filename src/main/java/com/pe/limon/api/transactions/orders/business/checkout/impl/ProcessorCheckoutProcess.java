package com.pe.limon.api.transactions.orders.business.checkout.impl;

import com.pe.limon.api.core.utils.exception.BusinessException;
import com.pe.limon.api.core.utils.exception.InternalServerException;
import com.pe.limon.api.transactions.orders.business.checkout.models.CheckoutContext;
import com.pe.limon.api.transactions.orders.business.checkout.models.CheckoutProcessor;
import com.pe.limon.api.transactions.orders.business.checkout.IProcessCheckout;
import com.pe.limon.api.transactions.orders.business.checkout.models.CheckoutResponse;
import com.pe.limon.api.transactions.orders.business.checkout.models.PaymentMode;
import com.pe.limon.api.transactions.payment.dto.generic.GenericPaymentDTO;
import com.pe.limon.api.transactions.payment.processors.ProcessorFactory;

import com.pe.limon.api.transactions.wallet.business.TransactionService;
import com.pe.limon.api.transactions.wallet.repository.WalletRepository;
import com.pe.limon.api.transactions.wallet.repository.entity.WalletEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("processor")
@RequiredArgsConstructor
public class ProcessorCheckoutProcess implements IProcessCheckout {

    private final TransactionService transactionService;
    private final ProcessorFactory processorFactory;
    private final WalletRepository walletRepository;

    @Override
    public PaymentMode mode() {
        return PaymentMode.PROCESSOR;
    }

    @Override
    public CheckoutResponse process(CheckoutContext context) {
        WalletEntity wallet = walletRepository.finByEventId(context.getOrder().getEventId());
        if (wallet == null) throw new BusinessException("The owner user not have a wallet with Lim-On.");

        CheckoutProcessor checkoutProcessor = (CheckoutProcessor) context;

        // 1) Llamada externa: SIN TX
        var processor = processorFactory.getProcessor(checkoutProcessor.getProcessor());
        var paymentResult = processor.execute(new GenericPaymentDTO(
                checkoutProcessor.getMethod(),
                checkoutProcessor.getProcessor(),
                checkoutProcessor.getOrder(),
                checkoutProcessor.getIp(),
                checkoutProcessor.getToken()
        ));

        if (!paymentResult.isSuccess()) throw new InternalServerException("Error al procesar pago: " + paymentResult.getMessage());

        CheckoutResponse checkoutResponse = new CheckoutResponse();
        checkoutResponse.setTransaction(transactionService.savePurchaseAndCommission(
                wallet,
                checkoutProcessor.getProcessor(),
                checkoutProcessor.getMethod(),
                context.getOrder(),
                paymentResult
        ));
        return checkoutResponse;

    }
}
