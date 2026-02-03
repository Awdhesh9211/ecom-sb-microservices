package com.ecommerce.orderms.service;



import com.ecommerce.orderms.dto.cart.request.CartItemRequest;
import com.ecommerce.orderms.dto.cart.response.CartItemResponse;

import java.util.List;
public interface CartService {

    boolean addToCart(String userId, CartItemRequest request);

//    boolean updateItemQuantity(String userId, String productId, int quantity);

    boolean deleteItemFromCart(String userId, String productId);

    boolean clearCart(String userId);

    List<CartItemResponse> getCart(String userId);

}

