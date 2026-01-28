package com.ecommerce.productms.service;



import com.ecommerce.productms.dto.request.ProductRequest;
import com.ecommerce.productms.dto.response.ProductResponse;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    List<ProductResponse> fetchAllProducts();
    boolean createProduct(ProductRequest productRequest);
    Optional<ProductResponse> getProduct(Long id);
    boolean updateProduct(ProductRequest productRequest,Long id);
    boolean deleteProduct(Long id);
    List<ProductResponse> searchProducts(String keyword);

}
