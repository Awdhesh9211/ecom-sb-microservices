package com.ecommerce.orderms.service.impl;

import com.ecommerce.orderms.clients.productclient.ProductServiceClient;
import com.ecommerce.orderms.clients.userclient.UserServiceClient;
import com.ecommerce.orderms.dto.cart.request.CartItemRequest;
import com.ecommerce.orderms.dto.cart.response.CartItemResponse;
import com.ecommerce.orderms.dto.product.ProductResponse;
import com.ecommerce.orderms.dto.user.response.UserResponse;
import com.ecommerce.orderms.model.cart.CartItem;
import com.ecommerce.orderms.repository.CartRepository;
import com.ecommerce.orderms.service.CartService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductServiceClient productServiceClient;
    private final UserServiceClient userServiceClient;
    private final Logger logger=LoggerFactory.getLogger(CartServiceImpl.class);
    int attempt=0;

    public CartServiceImpl(CartRepository cartRepository,ProductServiceClient productServiceClient,UserServiceClient userServiceClientInterface) {
        this.cartRepository = cartRepository;
        this.productServiceClient=productServiceClient;
        this.userServiceClient=userServiceClientInterface;
    }

    /* ---------------- Mapper -----------------------*/
    public CartItemResponse toResponse(CartItem cartItem) {

        CartItemResponse response = new CartItemResponse();

        response.setCartItemId(cartItem.getId());

        // User
        response.setUserId(cartItem.getUserId());

        // Product
        response.setProductId(cartItem.getProductId());
//        //neeed foreign call
        ProductResponse productResponse=productServiceClient.getProductDetails(Long.valueOf(cartItem.getProductId()));

        response.setProductName(productResponse.getName());
        response.setProductImage(productResponse.getImageUrl());
        response.setProductPrice(productResponse.getPrice());

        // Cart
        response.setQuantity(cartItem.getQuantity());
        BigDecimal totalPrice =productResponse.getPrice()
                        .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        response.setTotalPrice(totalPrice);

        return response;
    }

    /* ---------------- ADD TO CART ---------------- */

    // Resillience - CIRCUIT breaking  Logic
    @CircuitBreaker(name = "productService", fallbackMethod = "productFallback")
    public ProductResponse fetchProduct(Long productId) {
        try {
            return productServiceClient.getProductDetails(productId);
        } catch (Exception ex) {
            throw new RuntimeException("PRODUCT_SERVICE_DOWN");
        }
    }

    public ProductResponse productFallback(Long productId, Exception ex) {
        logger.error("Product service down for product {}", productId);
        throw new RuntimeException("PRODUCT_SERVICE_DOWN");
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "userFallback")
    public UserResponse fetchUser(String userId) {
        try {
            return userServiceClient.getUserDetails(userId);
        } catch (Exception ex) {
            throw new RuntimeException("USER_SERVICE_DOWN");
        }
    }

    public UserResponse userFallback(String userId, Exception ex) {
        logger.error("User service down for user {}", userId);
        throw new RuntimeException("USER_SERVICE_DOWN");
    }



    @Override
    @Retry(
            name = "retryService",
            fallbackMethod = "addToCartFallback"
    )
    public CartItem addToCart(String userId, CartItemRequest request) {

        System.out.println("ATTEMPT : " + ++attempt);

        // ---------- PRODUCT CALL ----------
        ProductResponse product;
        try {
            product = productServiceClient
                    .getProductDetails(Long.valueOf(request.getProductId()));
        }
        catch (org.springframework.web.client.HttpClientErrorException.NotFound ex) {
            // 404 → BUSINESS CASE (NO RETRY)
            throw new IllegalArgumentException("Product not found");
        }
        catch (Exception ex) {
            // Any OTHER error → service really down → RETRY
            throw new IllegalStateException("Product service unavailable", ex);
        }
        if (product.getPrice() == null)
            throw new IllegalArgumentException("Invalid product price"); // business → no retry
        // ----------------------------------

        // ---------- USER CALL ----------
        UserResponse user = userServiceClient.getUserDetails(userId);

        if (user == null)
            throw new IllegalArgumentException("User not found"); // business → no retry
        // ----------------------------------

        // ---------- CART LOGIC ----------
        CartItem cartItem = cartRepository.findByUserIdAndProductId(userId, request.getProductId());

        int finalQuantity = (cartItem == null) ? request.getQuantity() : cartItem.getQuantity() + request.getQuantity();

        if (finalQuantity <= 0 || product.getStockQuantity() < finalQuantity) {
            throw new IllegalArgumentException("Out of stock or invalid quantity"); // business → no retry
        }

        if (cartItem == null) {
            cartItem = new CartItem();
            cartItem.setUserId(userId);
            cartItem.setProductId(request.getProductId());
        }

        cartItem.setQuantity(finalQuantity);
        cartItem.setPrice(product.getPrice().multiply(BigDecimal.valueOf(finalQuantity)));

        return cartRepository.save(cartItem);
    }



    public Exception addToCartFallback(
            String userId,
            CartItemRequest request,
            Exception ex) {

        LoggerFactory.getLogger(CartService.class)
                .error("Fallback triggered: {}", ex.getMessage());

        // Return a dummy CartItem (or null if you prefer)
        throw null;
    }



    /* ---------------- DELETE SINGLE ITEM ---------------- */

    @Override
    @Transactional
    public boolean deleteItemFromCart(String userId, String productId) {


        // Look For product
        ProductResponse product=productServiceClient.getProductDetails(Long.valueOf(productId));

        if(product == null ) return false;

        // Look For user
        UserResponse user=userServiceClient.getUserDetails(userId);
        if(user == null) return false;


        int deleted = cartRepository.deleteByUserIdAndProductId(userId, productId);

        return deleted > 0;
    }


    /* ---------------- CLEAR CART ---------------- */

    @Override
    @Transactional
    public boolean clearCart(String userId) {
        return cartRepository.deleteByUserId(userId) > 0;
    }

    /* ---------------- GET CART ---------------- */

    @Override
    public List<CartItemResponse> getCart(String userId) {
        return  cartRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }


}