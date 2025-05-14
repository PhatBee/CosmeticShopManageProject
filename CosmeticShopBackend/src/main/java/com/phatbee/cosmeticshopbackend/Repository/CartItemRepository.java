package com.phatbee.cosmeticshopbackend.Repository;

import com.phatbee.cosmeticshopbackend.Entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
}
