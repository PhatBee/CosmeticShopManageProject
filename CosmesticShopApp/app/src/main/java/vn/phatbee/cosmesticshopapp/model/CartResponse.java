package vn.phatbee.cosmesticshopapp.model;

import com.google.gson.annotations.SerializedName;

public class CartResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("cart")
    private Cart cart;

    public CartResponse() {
    }

    public CartResponse(boolean success, String message, Cart cart) {
        this.success = success;
        this.message = message;
        this.cart = cart;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }
}