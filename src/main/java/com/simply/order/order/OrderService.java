package com.simply.order.order;

import com.simply.order.Kafka.OrderConfirmation;
import com.simply.order.Kafka.OrderProducer;
import com.simply.order.customer.CustomerClient;
import com.simply.order.exception.BusinessException;
import com.simply.order.orderline.OrderLineRequest;
import com.simply.order.orderline.OrderLineService;
import com.simply.order.payment.PaymentClient;
import com.simply.order.payment.PaymentRequest;
import com.simply.order.product.ProductClient;
import com.simply.order.product.PurchaseRequest;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final CustomerClient customerClient;
    private final PaymentClient paymentClient;
    private final ProductClient productClient;
    private final OrderLineService orderLineService;
    private final OrderProducer orderProducer;

//    @Transactional
//    public Integer createOrder(OrderRequest request){
//
//        var customer = customerClient.findCustomerById(request.customerId())
//                .orElseThrow(() -> new BusinessException("Cannot create order:: No customer exists with the provided ID"));
//
//        var purchasedProducts = productClient.purchaseProducts(request.products());
//
//        var order = repository.save(mapper.toOrder(request));
//
//        if (order.getReference() == null || order.getReference().isBlank()) {
//            order.setReference("ORD-" + java.util.UUID.randomUUID());
//        }
//
//        var savedOrder = repository.save(order);
//
//        for (PurchaseRequest purchaseRequest : request.products()) {
//            orderLineService.savedOrderLine(
//                    new OrderLineRequest(
//                            null,
//                            order.getId(),
//                            purchaseRequest.productId(),
//                            purchaseRequest.quantity()
//                    )
//            );
//        }
//
//        var paymentRequest = new PaymentRequest(
//                request.amount(),
//                request.paymentMethod(),
//                order.getId(),
//                order.getReference(),
//                customer
//        );
//
//        paymentClient.requestOrderPayment(paymentRequest);
//
//        orderProducer.sendOrderConfirmation(
//                new OrderConfirmation(
//                        request.reference(),
//                        request.amount(),
//                        request.paymentMethod(),
//                        customer,
//                        purchasedProducts
//                )
//        );
//
//        return order.getId();
//    }

    @Transactional
    public Integer createOrder(OrderRequest request){

        var customer = customerClient.findCustomerById(request.customerId())
                .orElseThrow(() -> new BusinessException("Cannot create order:: No customer exists with the provided ID"));

        var purchasedProducts = productClient.purchaseProducts(request.products());

        var order = mapper.toOrder(request);

        // ✅ generate reference if not provided
        if (order.getReference() == null || order.getReference().isBlank()) {
            order.setReference("ORD-" + java.util.UUID.randomUUID());
        }

        var savedOrder = repository.save(order);

        for (PurchaseRequest purchaseRequest : request.products()) {
            orderLineService.savedOrderLine(
                    new OrderLineRequest(
                            null,
                            savedOrder.getId(),
                            purchaseRequest.productId(),
                            purchaseRequest.quantity()
                    )
            );
        }

        var paymentRequest = new PaymentRequest(
                request.amount(),
                request.paymentMethod(),
                savedOrder.getId(),
                savedOrder.getReference(),   // ✅ now not null
                customer
        );

        paymentClient.requestOrderPayment(paymentRequest);

        orderProducer.sendOrderConfirmation(
                new OrderConfirmation(
                        savedOrder.getReference(),  // ✅ use saved reference
                        request.amount(),
                        request.paymentMethod(),
                        customer,
                        purchasedProducts
                )
        );

        return savedOrder.getId();
    }

    public List<OrderResponse> findAllOrders() {
        var orders = repository.findAll();
        List<OrderResponse> responses = new ArrayList<>();

        for (var order : orders) {
            responses.add(mapper.fromOrder(order));
        }
        return responses;
    }

    public OrderResponse findById(Integer id) {
        Order order = repository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Order not found with ID: " + id)
                );
        return mapper.fromOrder(order);
    }

}
