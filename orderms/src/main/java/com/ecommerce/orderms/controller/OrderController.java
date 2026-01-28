package com.ecommerce.orderms.controller;

import com.ecommerce.orderms.dto.order.response.OrderResponse;
import com.ecommerce.orderms.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
//@Tag(name = "Order", description = "Order management APIs")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestHeader("X-USER-ID") String userId){
        return orderService.createOrder(userId)
                .map(orderResponse -> new ResponseEntity<>(orderResponse, HttpStatus.CREATED))
                .orElseGet(()->ResponseEntity.badRequest().build());


    }

    // GET ALL USER ORDERS
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getUserOrders(
            @RequestHeader("X-User-ID") String userId) {

        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }



}
