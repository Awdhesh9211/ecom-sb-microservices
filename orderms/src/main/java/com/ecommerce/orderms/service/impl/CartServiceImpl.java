package com.ecommerce.orderms.service.impl;

import com.ecommerce.orderms.clients.productclient.ProductServiceClient;
import com.ecommerce.orderms.dto.cart.request.CartItemRequest;
import com.ecommerce.orderms.dto.cart.response.CartItemResponse;
import com.ecommerce.orderms.dto.product.ProductResponse;
import com.ecommerce.orderms.model.cart.CartItem;
import com.ecommerce.orderms.repository.CartRepository;
import com.ecommerce.orderms.service.CartService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductServiceClient productServiceClient;

    public CartServiceImpl(CartRepository cartRepository,ProductServiceClient productServiceClient) {
        this.cartRepository = cartRepository;
        this.productServiceClient=productServiceClient;
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
//        response.setProductName(cartItem.getProduct().getName());
//        response.setProductImage(cartItem.getProduct().getImageUrl());
//        response.setProductPrice(cartItem.getProduct().getPrice());

        // Cart
        response.setQuantity(cartItem.getQuantity());

// need call       BigDecimal totalPrice =
//                cartItem.getProductId()
//                        .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
//
//        response.setTotalPrice(totalPrice);

        return response;
    }

    /* ---------------- ADD TO CART ---------------- */

    @Override
    @Transactional
    public boolean addToCart(String userId, CartItemRequest request) {

           // Look For product
        ProductResponse productResponse=productServiceClient.getProductDetails(Long.valueOf(request.getProductId()));

        if(productResponse == null ) return false;

        if(productResponse.getStockQuantity() < request.getQuantity()) return false;







//        Optional<User> userOpt = userRepository.findById(uid);
//        Optional<Product> productOpt = productRepository.findById(request.getProductId());

//        if (userOpt.isEmpty() || productOpt.isEmpty())
//            return false;
//
//        User user = userOpt.get();
//        Product product = productOpt.get();

        CartItem cartItem = cartRepository.findByUserIdAndProductId(userId, request.getProductId());

        int finalQuantity = (cartItem == null)
                ? request.getQuantity()
                : cartItem.getQuantity() + request.getQuantity();

//        if (finalQuantity <= 0 || product.getStockQuantity() < finalQuantity)
//            return false;

        if (cartItem == null) {
            cartItem = new CartItem();
            cartItem.setUserId(userId);
            cartItem.setProductId(request.getProductId());
        }

        cartItem.setQuantity(finalQuantity);
//        cartItem.setPrice(calculatePrice(product, finalQuantity));

        cartRepository.save(cartItem);
        return true;
    }

    /* ---------------- DELETE SINGLE ITEM ---------------- */

    @Override
    @Transactional
    public boolean deleteItemFromCart(String userId, String productId) {

//        Long uid;
//        try {
//            uid = Long.valueOf(userId);
//        } catch (NumberFormatException e) {
//            return false;
//        }

//        Optional<User> userOpt = userRepository.findById(uid);
//        Optional<Product> productOpt = productRepository.findById(productId);

//        if (userOpt.isEmpty() || productOpt.isEmpty())
//            return false;

        int deleted = cartRepository.deleteByUserIdAndProductId(
                userId,
                productId
        );

        return deleted > 0;
    }


    /* ---------------- CLEAR CART ---------------- */

    @Override
    @Transactional
    public boolean clearCart(String userId) {

//        Long uid;
//        try {
//            uid = Long.valueOf(userId);
//        } catch (NumberFormatException e) {
//            return false;
//        }

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