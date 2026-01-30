package com.ecommerce.orderms.clients.userclient;


import com.ecommerce.orderms.dto.user.response.UserResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface UserServiceClient {

    @GetExchange("/api/v1/users/{id}")
    UserResponse getUserDetails(@PathVariable String id);
}
