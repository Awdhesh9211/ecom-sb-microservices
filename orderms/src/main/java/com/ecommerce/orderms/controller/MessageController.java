package com.ecommerce.orderms.controller;


import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


//for testing config server
@RestController
public class MessageController {


    @Value("${app.message}")
    private  String message;

    @GetMapping("/message")
    @Retry(name = "retryLimiter",fallbackMethod = "getMessageFallback")
    public String getMessage(){
        return message;
    }

    public String getMessageFallback(Exception ex){
        return "Try After Some Time ";
    }
}
