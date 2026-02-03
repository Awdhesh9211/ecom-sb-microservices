package com.ecommerce.orderms.controller;


import com.ecommerce.orderms.dto.cart.request.CartItemRequest;
import com.ecommerce.orderms.dto.cart.response.CartItemResponse;
import com.ecommerce.orderms.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;

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

        try{
            boolean added=cartService.addToCart(userId, request);
            if(!added){
                return ResponseEntity.badRequest().body("Not able to complete request ");
            }
        }catch(HttpClientErrorException.NotFound ex){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status","ERROR","message",ex.getMessage().split(":",2)));
        }
        catch (IllegalArgumentException ex) {
            HttpStatus status = ex.getMessage().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status)
                    .body(Map.of("status","FAILED","message", ex.getMessage()));

        } catch (Exception ex) {
            String msg=(ex instanceof IllegalStateException)
                    ? ex.getMessage().contains("user-service")
                    ? "USER SERVICE UNAVAILABLE " : "PRODUCT SERVICE UNAVAILABLE"
                    : ex.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status","ERROR","message",msg));
        }
        return ResponseEntity.ok("Added To Cart");
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
