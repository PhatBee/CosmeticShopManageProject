package com.phatbee.cosmeticshopbackend.Service.Impl;

import com.phatbee.cosmeticshopbackend.Entity.Product;
import com.phatbee.cosmeticshopbackend.Entity.User;
import com.phatbee.cosmeticshopbackend.Entity.Wishlist;
import com.phatbee.cosmeticshopbackend.Repository.ProductRepository;
import com.phatbee.cosmeticshopbackend.Repository.UserRepository;
import com.phatbee.cosmeticshopbackend.Repository.WishlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WishlistServiceImplTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private WishlistServiceImpl wishlistService;

    private User user;
    private Product product;
    private Wishlist wishlist;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setUserId(1L);

        product = new Product();
        product.setProductId(1L);
        product.setProductName("Lipstick");

        wishlist = new Wishlist();
        wishlist.setWishlistId(1L);
        wishlist.setUser(user);
        wishlist.setProduct(product);
    }

    @Test
    void addToWishlist_withValidUserAndProduct_addsSuccessfully() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(wishlistRepository.findByUserUserIdAndProductProductId(1L, 1L)).thenReturn(Optional.empty());
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(wishlist);

        // Act
        Wishlist result = wishlistService.addToWishlist(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertEquals(product, result.getProduct());
        verify(userRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).findById(1L);
        verify(wishlistRepository, times(1)).findByUserUserIdAndProductProductId(1L, 1L);
        verify(wishlistRepository, times(1)).save(any(Wishlist.class));
    }

    @Test
    void addToWishlist_withNonExistentUser_throwsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));


        // Act & Assert
        assertThrows(RuntimeException.class, () -> wishlistService.addToWishlist(1L, 1L), "User not found");
        verify(userRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).findById(1L);
        verify(wishlistRepository, never()).save(any(Wishlist.class));
    }

    @Test
    void addToWishlist_withNonExistentProduct_throwsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> wishlistService.addToWishlist(1L, 1L), "Product not found");
        verify(userRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).findById(1L);
        verify(wishlistRepository, never()).save(any(Wishlist.class));
    }

    @Test
    void addToWishlist_withExistingWishlist_throwsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(wishlistRepository.findByUserUserIdAndProductProductId(1L, 1L)).thenReturn(Optional.of(wishlist));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> wishlistService.addToWishlist(1L, 1L), "Product already in wishlist");
        verify(userRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).findById(1L);
        verify(wishlistRepository, times(1)).findByUserUserIdAndProductProductId(1L, 1L);
        verify(wishlistRepository, never()).save(any(Wishlist.class));
    }

    @Test
    void removeFromWishlist_withValidWishlist_removesSuccessfully() {
        // Arrange
        when(wishlistRepository.findByUserUserIdAndProductProductId(1L, 1L)).thenReturn(Optional.of(wishlist));

        // Act
        wishlistService.removeFromWishlist(1L, 1L);

        // Assert
        verify(wishlistRepository, times(1)).findByUserUserIdAndProductProductId(1L, 1L);
        verify(wishlistRepository, times(1)).delete(wishlist);
    }

    @Test
    void removeFromWishlist_withNonExistentWishlist_throwsException() {
        // Arrange
        when(wishlistRepository.findByUserUserIdAndProductProductId(1L, 1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> wishlistService.removeFromWishlist(1L, 1L), "Product not in wishlist");
        verify(wishlistRepository, times(1)).findByUserUserIdAndProductProductId(1L, 1L);
        verify(wishlistRepository, never()).delete(any(Wishlist.class));
    }

    @Test
    void getWishlistByUserId_withWishlist_returnsList() {
        // Arrange
        List<Wishlist> wishlists = Arrays.asList(wishlist);
        when(wishlistRepository.findByUserUserId(1L)).thenReturn(wishlists);

        // Act
        List<Wishlist> result = wishlistService.getWishlistByUserId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Lipstick", result.get(0).getProduct().getProductName());
        verify(wishlistRepository, times(1)).findByUserUserId(1L);
    }

    @Test
    void getWishlistByUserId_withNoWishlist_returnsEmptyList() {
        // Arrange
        when(wishlistRepository.findByUserUserId(1L)).thenReturn(Collections.emptyList());

        // Act
        List<Wishlist> result = wishlistService.getWishlistByUserId(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(wishlistRepository, times(1)).findByUserUserId(1L);
    }

    @Test
    void isProductInWishlist_withProductInWishlist_returnsTrue() {
        // Arrange
        when(wishlistRepository.findByUserUserIdAndProductProductId(1L, 1L)).thenReturn(Optional.of(wishlist));

        // Act
        boolean result = wishlistService.isProductInWishlist(1L, 1L);

        // Assert
        assertTrue(result);
        verify(wishlistRepository, times(1)).findByUserUserIdAndProductProductId(1L, 1L);
    }

    @Test
    void isProductInWishlist_withProductNotInWishlist_returnsFalse() {
        // Arrange
        when(wishlistRepository.findByUserUserIdAndProductProductId(1L, 1L)).thenReturn(Optional.empty());

        // Act
        boolean result = wishlistService.isProductInWishlist(1L, 1L);

        // Assert
        assertFalse(result);
        verify(wishlistRepository, times(1)).findByUserUserIdAndProductProductId(1L, 1L);
    }
}