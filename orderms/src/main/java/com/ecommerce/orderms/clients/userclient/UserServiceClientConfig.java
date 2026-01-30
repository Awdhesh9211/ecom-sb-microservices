package com.ecommerce.orderms.clients.userclient;


import com.ecommerce.orderms.clients.productclient.ProductServiceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.Optional;

@Configuration
public class UserServiceClientConfig {


    @Bean
    public UserServiceClient restClientInterface(RestClient.Builder builder){

        RestClient restClient= builder
                .baseUrl("http://user-service")
                .defaultStatusHandler(
                        HttpStatusCode::is4xxClientError,
                        (req,res)-> Optional.empty())
                .build();
        RestClientAdapter adapter=RestClientAdapter
                .create(restClient);
        HttpServiceProxyFactory factory= HttpServiceProxyFactory
                .builderFor(adapter).build();

        return factory.createClient(UserServiceClient.class);
    }
}
