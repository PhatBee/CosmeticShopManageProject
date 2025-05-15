package com.phatbee.cosmeticshopbackend.Service.Impl;

import com.phatbee.cosmeticshopbackend.Entity.Cart;
import com.phatbee.cosmeticshopbackend.Entity.CartItem;
import com.phatbee.cosmeticshopbackend.Entity.Product;
import com.phatbee.cosmeticshopbackend.Entity.User;
import com.phatbee.cosmeticshopbackend.Repository.CartItemRepository;
import com.phatbee.cosmeticshopbackend.Repository.CartRepository;
import com.phatbee.cosmeticshopbackend.Repository.ProductRepository;
import com.phatbee.cosmeticshopbackend.Repository.UserRepository;
import com.phatbee.cosmeticshopbackend.Service.CartService;
import com.phatbee.cosmeticshopbackend.dto.CartItemDTO;
import com.phatbee.cosmeticshopbackend.dto.CartItemRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Cart getCartByUserId(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get existing cart or create a new one if it doesn't exist
        Cart cart = cartRepository.findByCustomer_UserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setCustomer(user);
                    newCart.setCartItems(new HashSet<>());
                    return cartRepository.save(newCart);
                });

        return cart;
    }

    @Transactional
    @Override
    public Cart addItemToCart(Long userId, Long productId, Long quantity) {
        Cart cart = this.getCartByUserId(userId);
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Check if product already exists in cart
        Optional<CartItem> existingItemOpt = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getProductId().equals(productId))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            // Update quantity
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            cartItemRepository.save(existingItem);
        } else {
            // Add new item
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setCart(cart);
            cart.getCartItems().add(cartItemRepository.save(newItem));
        }

        return cartRepository.save(cart);
    }

    @Transactional
    @Override
    public Cart updateCartItemQuantity(Long userId, Long productId, Long quantity) {
        Cart cart = this.getCartByUserId(userId);

        CartItem itemToUpdate = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not in cart"));

        if (quantity <= 0) {
            cart.getCartItems().remove(itemToUpdate);
            cartItemRepository.delete(itemToUpdate);
        } else {
            itemToUpdate.setQuantity(quantity);
            cartItemRepository.save(itemToUpdate);
        }

        return cartRepository.save(cart);
    }

    @Transactional
    @Override
    public Cart removeItemFromCart(Long userId, Long cartItemId) {
        Cart cart = this.getCartByUserId(userId);

        CartItem itemToRemove = cart.getCartItems().stream()
                .filter(item -> item.getCartItemId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not in cart"));

        cart.getCartItems().remove(itemToRemove);
        cartItemRepository.delete(itemToRemove);

        return cartRepository.save(cart);
    }

    @Transactional
    @Override
    public void clearCart(Long userId) {
        Cart cart = this.getCartByUserId(userId);
        cartItemRepository.deleteAll(cart.getCartItems());
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }

    @Override
    public Cart addToCart(CartItemRequest request) {
        // Validate user
        Optional<User> userOptional = userRepository.findByUserId(request.getUserId());
        if (!userOptional.isPresent()) {
            throw new RuntimeException("User not found");
        }
        User user = userOptional.get();

        // Validate product
        Optional<Product> productOptional = productRepository.findByProductId(request.getProductId());
        if (!productOptional.isPresent()) {
            throw new RuntimeException("Product not found");
        }
        Product product = productOptional.get();

        // Find or create cart
        Cart cart = this.getCartByUserId(user.getUserId());
        if (cart == null) {

            cart = new Cart();
            cart.setCustomer(user);
            cart.setCartItems(new java.util.HashSet<>());
        }

        // Check if product is already in cart
        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getProductId().equals(request.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Update quantity
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        } else {
            // Add new cart item
            CartItem cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());
            cartItem.setCart(cart);
            cart.getCartItems().add(cartItem);
        }

        // Save cart
        return cartRepository.save(cart);
    }


    @Override
    public double calculateTotal(List<CartItemDTO> items) {
        return items.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
    }
}
