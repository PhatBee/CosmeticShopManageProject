package vn.phatbee.cosmesticshopapp.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Set;

public class Cart implements Serializable {
    @SerializedName("cartId")
    private Long cartId;

    @SerializedName("cartItems")
    private Set<CartItem> cartItems;

    @SerializedName("customer")
    private User customer;

    public Cart() {}

    public Cart(Long cartId, Set<CartItem> cartItems, User customer) {
        this.cartId = cartId;
        this.cartItems = cartItems;
        this.customer = customer;
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public Set<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(Set<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }
}