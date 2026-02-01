package com.ecommerce.userms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class MessageController {

    @Value("${app.message}")
    private  String message;


    @Autowired
    private Environment environment;


    @GetMapping("/message")
    public String getMessage(){
        return message + " | Port: " + environment.getProperty("local.server.port");
    }
}
