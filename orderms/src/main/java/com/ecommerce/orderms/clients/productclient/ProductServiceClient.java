package com.ecommerce.orderms.clients.productclient;

import com.ecommerce.orderms.dto.product.ProductResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface ProductServiceClient {

    @GetExchange("/api/v1/product/{id}")
    ProductResponse getProductDetails(@PathVariable Long id);
}
