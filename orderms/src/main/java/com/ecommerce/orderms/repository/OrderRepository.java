package com.ecommerce.orderms.repository;


import com.ecommerce.orderms.model.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {
    List<Order> findByUserId(String userId);
}
