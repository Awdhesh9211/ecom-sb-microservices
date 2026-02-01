package com.ecommerce.orderms.controller;


import com.ecommerce.orderms.dto.cart.request.CartItemRequest;
import com.ecommerce.orderms.dto.cart.response.CartItemResponse;
import com.ecommerce.orderms.model.cart.CartItem;
import com.ecommerce.orderms.service.CartService;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/cart")
//@Tag(name = "Cart", description = "Cart management APIs")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /* ---------------- ADD TO CART ---------------- */

    @PostMapping
    public ResponseEntity<Object> addToCart(
            @RequestHeader("X-User-ID") String userId,
            @RequestBody CartItemRequest request) {

        try {
            Optional<CartItem> result = cartService.addToCart(userId, request);
            System.out.println("Result :" + result.get());

            if (result.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "status","FAILED",
                                "message","Product out of stock or user not found"
                        ));
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "status","SUCCESS",
                            "message","Item added to cart"
                    ));

        } catch (Exception ex) {

            LoggerFactory.getLogger(CartService.class).error(ex.getMessage());

            String message;

            if ("USER_SERVICE_DOWN".equals(ex.getMessage())) {
                message = "User service temporarily unavailable";
            }
            else if ("PRODUCT_SERVICE_DOWN".equals(ex.getMessage())) {
                message = "Product service temporarily unavailable";
            }
            else {
                message = "Something went wrong";
            }

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "status","ERROR",
                            "message", message
                    ));
        }
    }




    /* ---------------- UPDATE QUANTITY ---------------- */

//    @PutMapping("/update/{productId}")
//    public ResponseEntity<String> updateQuantity(
//            @RequestHeader("X-User-ID") String userId,
//            @PathVariable Long productId,
//            @RequestParam int quantity) {
//
//        boolean updated =
//                cartService.updateItemQuantity(userId, productId, quantity);
//
//        if (!updated)
//            return ResponseEntity
//                    .badRequest()
//                    .body("Unable to update quantity");
//
//        return ResponseEntity.ok("Quantity updated");
//    }

    /* ---------------- DELETE SINGLE ITEM ---------------- */

    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<Void> removeFromCart(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable Long productId) {

        boolean deleted =
                cartService.deleteItemFromCart(userId, String.valueOf(productId));

        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }


    /* ---------------- CLEAR CART ---------------- */

    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart(
            @RequestHeader("X-User-ID") String userId) {

        boolean cleared = cartService.clearCart(userId);

        if (!cleared)
            return ResponseEntity
                    .badRequest()
                    .body("Cart already empty");

        return ResponseEntity.ok("Cart cleared");
    }

    /* ---------------- GET CART ---------------- */

    @GetMapping
    public ResponseEntity<List<CartItemResponse>> getCart(
            @RequestHeader("X-User-ID") String userId) {

        return ResponseEntity.ok(
                cartService.getCart(userId)
        );
    }
}
