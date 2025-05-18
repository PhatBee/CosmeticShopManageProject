package com.phatbee.cosmeticshopbackend.Service.Impl;

import com.phatbee.cosmeticshopbackend.dto.CartItemRequest;
import com.phatbee.cosmeticshopbackend.Entity.Cart;
import com.phatbee.cosmeticshopbackend.Entity.CartItem;
import com.phatbee.cosmeticshopbackend.Entity.Product;
import com.phatbee.cosmeticshopbackend.Entity.User;
import com.phatbee.cosmeticshopbackend.Repository.CartItemRepository;
import com.phatbee.cosmeticshopbackend.Repository.CartRepository;
import com.phatbee.cosmeticshopbackend.Repository.ProductRepository;
import com.phatbee.cosmeticshopbackend.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.elasticsearch.ResourceNotFoundException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private User user;
    private Product product;
    private Cart cart;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setUserId(1L);

        product = new Product();
        product.setProductId(1L);

        cart = new Cart();
        cart.setCartId(1L);
        cart.setCustomer(user);
        cart.setCartItems(new HashSet<>());

        cartItem = new CartItem();
        cartItem.setCartItemId(1L);
        cartItem.setProduct(product);
        cartItem.setQuantity(2L);
        cartItem.setCart(cart);
    }

    // Thêm sản phẩm vào giỏ hàng (3.4.17) - addToCart
    @Test
    void addToCart_withNewProduct_addsToCart() {
        // Arrange
        CartItemRequest request = new CartItemRequest();
        request.setUserId(1L);
        request.setProductId(1L);
        request.setQuantity(2L);

        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(user));
        when(productRepository.findByProductId(1L)).thenReturn(Optional.of(product));
        when(cartRepository.findByCustomer_UserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // Act
        Cart result = cartService.addToCart(request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getCartItems().size());
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void addToCart_withExistingProduct_updatesQuantity() {
        // Arrange
        CartItemRequest request = new CartItemRequest();
        request.setUserId(1L);
        request.setProductId(1L);
        request.setQuantity(3L);

        cart.getCartItems().add(cartItem);
        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(user));
        when(productRepository.findByProductId(1L)).thenReturn(Optional.of(product));
        when(cartRepository.findByCustomer_UserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // Act
        Cart result = cartService.addToCart(request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getCartItems().size());
        assertEquals(5L, result.getCartItems().iterator().next().getQuantity());
        verify(cartRepository, times(1)).save(cart);
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addToCart_withNonExistentUser_throwsException() {
        // Arrange
        CartItemRequest request = new CartItemRequest();
        request.setUserId(999L);
        request.setProductId(1L);
        request.setQuantity(2L);

        when(userRepository.findByUserId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> cartService.addToCart(request), "User not found");
        verify(cartRepository, never()).save(any(Cart.class));
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addToCart_withNonExistentProduct_throwsException() {
        // Arrange
        CartItemRequest request = new CartItemRequest();
        request.setUserId(1L);
        request.setProductId(999L);
        request.setQuantity(2L);

        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(user));
        when(productRepository.findByProductId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> cartService.addToCart(request), "Product not found");
        verify(cartRepository, never()).save(any(Cart.class));
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    // Thêm sản phẩm vào giỏ hàng (3.4.17) - addItemToCart
    @Test
    void addItemToCart_withNewProduct_addsToCart() {
        // Arrange
        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(user));
        when(productRepository.findByProductId(1L)).thenReturn(Optional.of(product));
        when(cartRepository.findByCustomer_UserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // Act
        Cart result = cartService.addItemToCart(1L, 1L, 2L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getCartItems().size());
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void addItemToCart_withExistingProduct_updatesQuantity() {
        // Arrange
        cart.getCartItems().add(cartItem);
        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(user));
        when(productRepository.findByProductId(1L)).thenReturn(Optional.of(product));
        when(cartRepository.findByCustomer_UserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // Act
        Cart result = cartService.addItemToCart(1L, 1L, 3L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getCartItems().size());
        assertEquals(5L, cartItem.getQuantity());
        verify(cartItemRepository, times(1)).save(cartItem);
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void addItemToCart_withNonExistentProduct_throwsException() {
        // Arrange
        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByCustomer_UserId(1L)).thenReturn(Optional.of(cart)); // Thêm mock
        when(productRepository.findByProductId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> cartService.addItemToCart(1L, 999L, 2L), "Product not found");
        verify(cartRepository, never()).save(any(Cart.class));
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    // Cập nhật số lượng sản phẩm trong giỏ hàng (3.4.18)
    @Test
    void updateCartItemQuantity_withValidQuantity_updatesQuantity() {
        // Arrange
        cart.getCartItems().add(cartItem);
        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByCustomer_UserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // Act
        Cart result = cartService.updateCartItemQuantity(1L, 1L, 5L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getCartItems().size());
        assertEquals(5L, cartItem.getQuantity());
        verify(cartItemRepository, times(1)).save(cartItem);
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void updateCartItemQuantity_withZeroQuantity_removesItem() {
        // Arrange
        cart.getCartItems().add(cartItem);
        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByCustomer_UserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // Act
        Cart result = cartService.updateCartItemQuantity(1L, 1L, 0L);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getCartItems().size());
        verify(cartItemRepository, times(1)).delete(cartItem);
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void updateCartItemQuantity_withNonExistentProduct_throwsException() {
        // Arrange
        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByCustomer_UserId(1L)).thenReturn(Optional.of(cart)); // Thêm mock

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> cartService.updateCartItemQuantity(1L, 999L, 5L), "Item not in cart");
        verify(cartItemRepository, never()).save(any(CartItem.class));
        verify(cartRepository, never()).save(any(Cart.class));
    }

    // Xóa sản phẩm khỏi giỏ hàng (3.4.19)
    @Test
    void removeItemFromCart_withValidCartItemId_removesItem() {
        // Arrange
        cart.getCartItems().add(cartItem);
        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByCustomer_UserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // Act
        Cart result = cartService.removeItemFromCart(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getCartItems().size());
        verify(cartItemRepository, times(1)).delete(cartItem);
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void removeItemFromCart_withNonExistentCartItemId_throwsException() {
        // Arrange
        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByCustomer_UserId(1L)).thenReturn(Optional.of(cart)); // Thêm mock

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> cartService.removeItemFromCart(1L, 999L), "Item not in cart");
        verify(cartItemRepository, never()).delete(any(CartItem.class));
        verify(cartRepository, never()).save(any(Cart.class));
    }
}