package vn.phatbee.cosmesticshopapp.model;

import java.io.Serializable;

public class CartItemLite implements Serializable {
    private Long cartItemId;
    private Long productId;
    private Long quantity;

    public CartItemLite(Long cartItemId, Long productId, Long quantity) {
        this.cartItemId = cartItemId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getCartItemId() {
        return cartItemId;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getQuantity() {
        return quantity;
    }
}