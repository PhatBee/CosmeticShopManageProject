package vn.phatbee.cosmesticshopapp.model;

import java.io.Serializable;

public class Wishlist implements Serializable {
    private Long wishlistId;
    private User user;
    private Product product;

    // Getters and setters
    public Long getWishlistId() {
        return wishlistId;
    }

    public void setWishlistId(Long wishlistId) {
        this.wishlistId = wishlistId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}