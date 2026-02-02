package com.ecommerce.apigateway.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    @LoadBalanced
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r
                        .path("/api/v1/users/**")
                        .filters(f->
                                f.circuitBreaker(config -> config
                                        .setName("ecomBreaker")
                                        .setFallbackUri("froward:/fallback/gateway/user")
                                )

                        )
                        .uri("lb://USER-SERVICE"))
                .route("product-service", r -> r
                        .path("/api/v1/product/**")
                        .filters(f->
                                f.circuitBreaker(config -> config
                                        .setName("ecomBreaker")
                                        .setFallbackUri("froward:/fallback/gateway/product")
                                )

                        )
                        .uri("lb://PRODUCT-SERVICE"))
                .route("order-service", r -> r
                        .path("/api/v1/order/**")
                        .filters(f->
                                f.circuitBreaker(config -> config
                                        .setName("ecomBreaker")
                                        .setFallbackUri("froward:/fallback/gateway/order")
                                )

                        )
                        .uri("lb://ORDER-SERVICE"))
                .route("cart-service", r -> r
                        .path("/api/v1/cart/**")
                        .filters(f->
                                f.circuitBreaker(config -> config
                                        .setName("ecomBreaker")
                                        .setFallbackUri("froward:/fallback/gateway/order")
                                )

                        )
                        .uri("lb://ORDER-SERVICE"))
                .route("eureka-server", r -> r
                        .path("/eureka/main")
                        .filters(f -> f.setPath("/"))
                        .uri("http://localhost:8761"))
                .route("eureka-server-static", r -> r
                        .path("/eureka/**")
                        .uri("http://localhost:8761"))
                .route("ectuator", r -> r
                        .path("/actuator/**")
                        .uri("http://localhost:8080/actuator"))
                .build();
    }
}
