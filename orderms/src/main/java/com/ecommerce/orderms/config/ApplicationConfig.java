package com.ecommerce.orderms.config;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

@Configuration
public class ApplicationConfig {

    @Autowired(required = false)
    private ObservationRegistry observationRegistry;

    @Autowired(required = false)
    private Tracer tracer;

    @Autowired(required = false)
    private Propagator propagator;

    private ClientHttpRequestInterceptor createTracingInterceptor() {
        return (HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
                -> {
            if (tracer != null && propagator != null && tracer.currentSpan() != null) {

                // Inject trace headers into the outgoing request
                propagator.inject(
                        tracer.currentSpan().context(),
                        request.getHeaders(),
                        (headers, key, value) -> headers.add(key, value)
                );
            }
            return execution.execute(request, body);
        };
    }

    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {

        RestClient.Builder builder = RestClient.builder();

        if (observationRegistry != null && tracer != null && propagator != null) {
            builder.requestInterceptor(createTracingInterceptor());
        }

        return builder;
    }

    @Bean
    @Primary
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
