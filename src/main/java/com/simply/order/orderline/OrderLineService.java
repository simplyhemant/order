package com.simply.order.orderline;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderLineService {

    private final OrderLineRepository repository;
    private final OrderLineMapper mapper;

    public Integer savedOrderLine(OrderLineRequest request) {
        var orderLine = mapper.toOrderLine(request);
        return repository.save(orderLine).getId();
    }

    public List<OrderLineResponse> findAllByOrderId(Integer orderId) {

        List<OrderLineResponse> responses = new ArrayList<>();

        for (var orderLine : repository.findAllByOrderId(orderId)) {
            responses.add(mapper.toOrderLineResponse(orderLine));
        }

        return responses;
    }

}
