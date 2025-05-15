package com.phatbee.cosmeticshopbackend.Service;

import com.phatbee.cosmeticshopbackend.Entity.Cart;
import com.phatbee.cosmeticshopbackend.dto.CartItemDTO;
import com.phatbee.cosmeticshopbackend.dto.CartItemRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CartService {
    Cart getCartByUserId(Long userId);

    @Transactional
    Cart addItemToCart(Long userId, Long productId, Long quantity);

    @Transactional
    Cart updateCartItemQuantity(Long userId, Long productId, Long quantity);

    @Transactional
    Cart removeItemFromCart(Long userId, Long cartItemId);

    @Transactional
    void clearCart(Long userId);

    Cart addToCart(CartItemRequest request);

    double calculateTotal(List<CartItemDTO> items);
}
