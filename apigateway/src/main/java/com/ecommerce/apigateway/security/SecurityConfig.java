package com.ecommerce.apigateway.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {


    @Bean
    public SecurityWebFilterChain  securityWebFilterChain(ServerHttpSecurity http){
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange-> exchange
                        .pathMatchers("/api/v1/product/**").hasRole("PRODUCT")
                        .pathMatchers("/api/v1/order/**").hasRole("ORDER")
                        .pathMatchers("/api/v1/cart/**").hasRole("ORDER")
                        .pathMatchers("/api/v1/users/**").hasRole("USER")
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2->oauth2.jwt(jwt->jwt.jwtAuthenticationConverter(jwtMonoConverter())))
                .build();
    }

    // ===== KEYCLOAK ROLE EXTRACTING (Reactive) USING STREAMS =====
    private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtMonoConverter() {

        ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            // Extract ONLY ecom-app Client Roles
            var clientRoles = Optional.ofNullable((Map<String, Object>) jwt.getClaims().get("resource_access"))
                    .map(m -> (Map<?, ?>) m.get("oauth2-pkce"))
                    .map(m -> (Collection<?>) m.get("roles"))
                    .orElse(List.of())
                    .stream()
                    .map(Object::toString)
                    .toList();

            System.out.println("EXTRACTED ROLES :"+ " "+clientRoles);

            // Merge + convert to Spring Authorities
            return Flux.fromIterable(clientRoles)
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role));
        });

        return converter;
    }


}
