package com.ecommerce.productms.service.impl;


import com.ecommerce.productms.dto.request.ProductRequest;
import com.ecommerce.productms.dto.response.ProductResponse;
import com.ecommerce.productms.model.Product;
import com.ecommerce.productms.repository.ProductRepository;
import com.ecommerce.productms.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    //Mapper
      //entity->response
    private ProductResponse mapToProductResponse(Product product) {

        ProductResponse response = new ProductResponse();

        response.setId(String.valueOf(product.getId()));
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setCategory(product.getCategory());
        response.setImageUrl(product.getImageUrl());
        response.setActive(product.getActive());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        return response;
    }
      // Request -> entity
      private void mapProductRequestToProduct(ProductRequest request, Product product) {

          Optional.ofNullable(request.getName())
                  .ifPresent(product::setName);

          Optional.ofNullable(request.getDescription())
                  .ifPresent(product::setDescription);

          Optional.ofNullable(request.getPrice())
                  .ifPresent(product::setPrice);

          Optional.ofNullable(request.getStockQuantity())
                  .ifPresent(product::setStockQuantity);

          Optional.ofNullable(request.getCategory())
                  .ifPresent(product::setCategory);

          Optional.ofNullable(request.getImageUrl())
                  .ifPresent(product::setImageUrl);

          Optional.ofNullable(request.getActive())
                  .ifPresent(product::setActive);
      }




    //LOGIC
     //CREATE
    public boolean createProduct(ProductRequest request) {
        try {
            Product product = new Product();
            mapProductRequestToProduct(request, product);
            productRepository.save(product);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //UPDATE - PATCH MANNER
    public boolean updateProduct(ProductRequest productRequest, Long id) {

        return productRepository.findById(id)
                .map(product -> {
                    mapProductRequestToProduct(productRequest,product);
                    productRepository.save(product);
                    return true;
                })
                .orElse(false);
    }
      //FETCH ALL
      public List<ProductResponse> fetchAllProducts(){
          return productRepository.findByActiveTrue().stream()
                  .map(this::mapToProductResponse)
                  .collect(Collectors.toList());
      }
       //GET PRODUCT BY ID
       public Optional<ProductResponse> getProduct(Long id){
           return productRepository.findById(id)
                   .map(u->mapToProductResponse(u));
       }

        //DELETE
        public boolean deleteProduct(Long id){
            return productRepository.findById(id)
                    .map(product -> {
                        product.setActive(false);
                        productRepository.save(product);
                        return true;
                    }).orElse(false);
        }

    @Override
    public List<ProductResponse> searchProducts(String keyword) {
        return productRepository.searchProducts(keyword).stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());

    }
}
