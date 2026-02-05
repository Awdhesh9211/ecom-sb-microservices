package com.ecommerce.notification.consumer;


import com.ecommerce.notification.dto.order.response.OrderResponse;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class OrderEventConsumer {


    @Bean
    public Consumer<OrderResponse> orderCreated(){
       return event->{
           LoggerFactory.getLogger(OrderEventConsumer.class).info("ID : "+ event.getId());
       };
    }

}
