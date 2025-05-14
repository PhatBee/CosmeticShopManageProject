package vn.phatbee.cosmesticshopapp.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CartItem implements Serializable {
    @SerializedName("cartItemId")
    private Long cartItemId;

    @SerializedName("product")
    private Product product;

    @SerializedName("quantity")
    private Long quantity;

    @SerializedName("cart")
    private Cart cart;

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public Long getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(Long cartItemId) {
        this.cartItemId = cartItemId;
    }
}