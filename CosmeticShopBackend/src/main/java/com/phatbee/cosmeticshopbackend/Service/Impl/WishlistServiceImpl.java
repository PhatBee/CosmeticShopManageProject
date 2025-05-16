package com.phatbee.cosmeticshopbackend.Service.Impl;

import com.phatbee.cosmeticshopbackend.Entity.Product;
import com.phatbee.cosmeticshopbackend.Entity.User;
import com.phatbee.cosmeticshopbackend.Entity.Wishlist;
import com.phatbee.cosmeticshopbackend.Repository.ProductRepository;
import com.phatbee.cosmeticshopbackend.Repository.UserRepository;
import com.phatbee.cosmeticshopbackend.Repository.WishlistRepository;
import com.phatbee.cosmeticshopbackend.Service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WishlistServiceImpl implements WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Wishlist addToWishlist(Long userId, Long productId) {
        Optional<User> userOptional = userRepository.findById(userId);
        Optional<Product> productOptional = productRepository.findById(productId);

        if (userOptional.isEmpty() || productOptional.isEmpty()) {
            throw new RuntimeException("Product not found");
        }

        Optional<Wishlist> existingWishlist = wishlistRepository.findByUserUserIdAndProductProductId(userId, productId);
        if (existingWishlist.isPresent()) {
            throw new RuntimeException("Product already in wishlist");
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(userOptional.get());
        wishlist.setProduct(productOptional.get());
        return wishlistRepository.save(wishlist);
    }

    @Override
    public void removeFromWishlist(Long userId, Long productId) {
        Optional<Wishlist> wishlistOptional = wishlistRepository.findByUserUserIdAndProductProductId(userId, productId);
        if (wishlistOptional.isPresent()) {
            wishlistRepository.delete(wishlistOptional.get());
        } else {
            throw new RuntimeException("Product not in wishlist");
        }
    }

    @Override
    public List<Wishlist> getWishlistByUserId(Long userId) {
        return wishlistRepository.findByUserUserId(userId);
    }

    @Override
    public boolean isProductInWishlist(Long userId, Long productId) {
        return wishlistRepository.findByUserUserIdAndProductProductId(userId, productId).isPresent();
    }
}