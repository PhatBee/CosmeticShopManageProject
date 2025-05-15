package com.phatbee.cosmeticshopbackend.Controller;

import com.phatbee.cosmeticshopbackend.Entity.Cart;
import com.phatbee.cosmeticshopbackend.Service.Impl.CartServiceImpl;
import com.phatbee.cosmeticshopbackend.dto.CartItemRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    @Autowired
    CartServiceImpl cartService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<Cart> getUserCart(@PathVariable Long userId) {
        Cart cart = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/update")
    public ResponseEntity<Cart> updateCartItem(@RequestBody CartItemRequest request) {
        Cart updatedCart = cartService.updateCartItemQuantity(request.getUserId(), request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/remove/{userId}/{cartItemId}")
    public ResponseEntity<Cart> removeFromCart(@PathVariable Long userId, @PathVariable Long cartItemId) {
        Cart updatedCart = cartService.removeItemFromCart(userId, cartItemId);
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/clear/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/add")
    public ResponseEntity<Cart> addToCart(@RequestBody CartItemRequest request) {
        try {
            Cart updatedCart = cartService.addToCart(request);
            return ResponseEntity.ok(updatedCart);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
