package com.ecommerce.orderms.service;


import com.ecommerce.orderms.dto.order.response.OrderResponse;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    Optional<OrderResponse> createOrder(String userId);
    List<OrderResponse> getOrdersByUserId(String userId);
}
