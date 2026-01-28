package com.ecommerce.orderms.repository;


import com.ecommerce.orderms.model.cart.CartItem;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartRepository extends JpaRepository<CartItem,Long> {
    CartItem findByUserIdAndProductId(String userId, String productId);
    int deleteByUserIdAndProductId(String userId, String productId);
    List<CartItem> findByUserId(String userId);
    int deleteByUserId(String userId);
}
