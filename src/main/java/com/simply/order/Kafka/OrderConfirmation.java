package com.simply.order.Kafka;

import com.simply.order.customer.CustomerResponse;
import com.simply.order.order.PaymentMethod;
import com.simply.order.product.PurchaseResponse;

import java.math.BigDecimal;
import java.util.List;

public record OrderConfirmation (
        String orderReference,
        BigDecimal totalAmount,
        PaymentMethod paymentMethod,
        CustomerResponse customer,
        List<PurchaseResponse> products

) {
}
