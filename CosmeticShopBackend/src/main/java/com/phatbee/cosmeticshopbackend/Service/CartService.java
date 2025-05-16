package com.phatbee.cosmeticshopbackend.Service;

import com.phatbee.cosmeticshopbackend.Entity.Cart;
import com.phatbee.cosmeticshopbackend.dto.CartItemRequest;

public interface CartService {
    Cart getCartByUserId(Long userId);
    Cart addItemToCart(Long userId, Long productId, Long quantity);
    Cart updateCartItemQuantity(Long userId, Long productId, Long quantity);
    Cart removeItemFromCart(Long userId, Long cartItemId);
    void clearCart(Long userId);
    Cart addToCart(CartItemRequest request);
}
