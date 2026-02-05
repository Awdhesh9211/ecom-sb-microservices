package com.ecommerce.notification.consumer;


import com.ecommerce.notification.dto.order.response.OrderResponse;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class OrderEventConsumer {


    @RabbitListener(queues = "order.queue")
    public void handleOrderEvent(OrderResponse orderResponse){
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(orderResponse));

    }
}
