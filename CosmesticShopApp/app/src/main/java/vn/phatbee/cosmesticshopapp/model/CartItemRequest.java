package vn.phatbee.cosmesticshopapp.model;

public class CartItemRequest {
    private Long userId;
    private Long productId;
    private Long quantity;
    private Long cartItemId;

    public CartItemRequest() {
    }

    public CartItemRequest(Long userId, Long productId, Long quantity) {
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public CartItemRequest(Long userId, Long productId, Long quantity, Long cartItemId) {
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.cartItemId = cartItemId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public Long getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(Long cartItemId) {
        this.cartItemId = cartItemId;
    }
}