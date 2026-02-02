package com.ecommerce.orderms.service;



import com.ecommerce.orderms.dto.cart.request.CartItemRequest;
import com.ecommerce.orderms.dto.cart.response.CartItemResponse;
import com.ecommerce.orderms.model.cart.CartItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CartService {

    CartItem addToCart(String userId, CartItemRequest request);

//    boolean updateItemQuantity(String userId, String productId, int quantity);

    boolean deleteItemFromCart(String userId, String productId);

    boolean clearCart(String userId);

    List<CartItemResponse> getCart(String userId);

}

