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
import jakarta.transaction.Transactional;
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

    @Override
    @Transactional
    public boolean addToCart(String userId, CartItemRequest request) {

           // Look For product
        ProductResponse product=productServiceClient.getProductDetails(Long.valueOf(request.getProductId()));

        if(product == null ) return false;

        if(product.getStockQuantity() < request.getQuantity()) return false;

        if (product.getPrice() == null) return false;

          // Look For user
        UserResponse user=userServiceClient.getUserDetails(userId);
        if(user == null) return false;

        CartItem cartItem = cartRepository.findByUserIdAndProductId(userId, request.getProductId());

        int finalQuantity = (cartItem == null)
                ? request.getQuantity()
                : cartItem.getQuantity() + request.getQuantity();

        if (finalQuantity <= 0 || product.getStockQuantity() < finalQuantity)
            return false;

        if (cartItem == null) {
            cartItem = new CartItem();
            cartItem.setUserId(userId);
            cartItem.setProductId(request.getProductId());
        }

        cartItem.setQuantity(finalQuantity);
        cartItem.setPrice(product.getPrice().multiply(BigDecimal.valueOf(finalQuantity)));

        cartRepository.save(cartItem);
        return true;
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