package com.phatbee.cosmeticshopbackend.Service;

import com.phatbee.cosmeticshopbackend.Entity.Wishlist;

import java.util.List;

public interface WishlistService {
    Wishlist addToWishlist(Long userId, Long productId);
    void removeFromWishlist(Long userId, Long productId);
    List<Wishlist> getWishlistByUserId(Long userId);
    boolean isProductInWishlist(Long userId, Long productId);
}