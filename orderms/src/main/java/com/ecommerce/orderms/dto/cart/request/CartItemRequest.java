package com.ecommerce.orderms.dto.cart.request;

public class CartItemRequest {
    private String productId;
    private Integer quantity;

    //CONSTRUCTOR GETTER SETTER

    public CartItemRequest() {

    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
