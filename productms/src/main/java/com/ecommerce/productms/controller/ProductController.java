package com.ecommerce.productms.controller;



import com.ecommerce.productms.dto.request.ProductRequest;
import com.ecommerce.productms.dto.response.ProductResponse;
import com.ecommerce.productms.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
//@Tag(name = "Product", description = "Product management APIs")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(){
        return ResponseEntity.ok(productService.fetchAllProducts());
    }

    @PostMapping
    public ResponseEntity<String> createProduct(@RequestBody ProductRequest productRequest){
        if(productService.createProduct(productRequest)){
            return ResponseEntity.ok("Product Added Successfull");
        }
        return new ResponseEntity<>("Problem in Adding", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long  id){
        return productService.getProduct(id)
                .map(ResponseEntity::ok)
                .orElseGet(()->ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateProduct(@PathVariable Long  id,@RequestBody ProductRequest productRequest){
        boolean updated= productService.updateProduct(productRequest,id);
        if(updated)
            return ResponseEntity.ok("Updated Successfully !");

        return ResponseEntity.notFound().build();

    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long  id){
        boolean updated= productService.deleteProduct(id);
        if(updated)
            return ResponseEntity.ok("Deleted Successfully !");

        return ResponseEntity.notFound().build();

    }

    @GetMapping("/search/{keyword}")
    public ResponseEntity<List<ProductResponse>>  searchproduct(@PathVariable String keyword){
        return ResponseEntity.ok(productService.searchProducts(keyword));
    }
}
