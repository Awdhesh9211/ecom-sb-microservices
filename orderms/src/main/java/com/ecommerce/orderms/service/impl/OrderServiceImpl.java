package com.ecommerce.orderms.service.impl;



import com.ecommerce.orderms.clients.userclient.UserServiceClient;
import com.ecommerce.orderms.dto.order.response.OrderItemDTO;
import com.ecommerce.orderms.dto.order.response.OrderResponse;
import com.ecommerce.orderms.dto.user.response.UserResponse;
import com.ecommerce.orderms.enumclass.OrderStatus;
import com.ecommerce.orderms.model.cart.CartItem;
import com.ecommerce.orderms.model.order.Order;
import com.ecommerce.orderms.model.order.OrderItem;
import com.ecommerce.orderms.repository.CartRepository;
import com.ecommerce.orderms.repository.OrderRepository;
import com.ecommerce.orderms.service.OrderService;
import jakarta.transaction.Transactional;
import org.apache.catalina.User;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final UserServiceClient userServiceClient;
    private final RabbitTemplate rabbitTemplate;

    public OrderServiceImpl(CartRepository cartRepository, OrderRepository orderRepository,UserServiceClient userServiceClient,RabbitTemplate rabbitTemplate) {
        this.cartRepository = cartRepository;
        this.orderRepository=orderRepository;
        this.userServiceClient=userServiceClient;
        this.rabbitTemplate = rabbitTemplate;
    }

    ///  MAPPER
    private OrderResponse mapToOrderResponse(Order saveOrder) {
        return new OrderResponse(
                saveOrder.getId(),
                saveOrder.getTotalAmount(),
                saveOrder.getStatus(),
                saveOrder.getItems().stream()
                        .map(orderItem -> new OrderItemDTO(
                                orderItem.getId(),
                                orderItem.getProductId(),
                                orderItem.getQuantity(),
                                orderItem.getPrice(),
                                orderItem.getPrice().multiply(new BigDecimal(orderItem.getQuantity()))
                        )).toList(),
                saveOrder.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public Optional<OrderResponse> createOrder(String userId) {
        // Validate for cart item
        List<CartItem> cartItems=cartRepository.findByUserId(userId);
        if(cartItems.isEmpty()){
            return Optional.empty();
        }

        UserResponse user=userServiceClient.getUserDetails(userId);
        if(user == null) return Optional.empty();


        // Calculate total price
        BigDecimal totalPrice=cartItems.stream()
                .map(CartItem::getPrice)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        // Create order
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setTotalAmount(totalPrice);
        List<OrderItem> orderItems=cartItems.stream()
                .map(item->new OrderItem(
                        null,
                        item.getProductId(),
                        item.getQuantity(),
                        item.getPrice(),
                        order
                )).toList();
        order.setItems(orderItems);
        Order saveOrder=orderRepository.save(order);
        // Clear the cart
        cartRepository.deleteByUserId(userId);
        OrderResponse orderResponse=mapToOrderResponse(saveOrder);
        // produce notification
        rabbitTemplate.convertAndSend(
                exchangeName,
                routingKey,
                orderResponse
        );
        return Optional.of(orderResponse);
    }

    @Override
    public List<OrderResponse> getOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId)
                .stream()
                .map(this::mapToOrderResponse)
                .toList();
    }



}
