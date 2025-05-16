package com.phatbee.cosmeticshopbackend.Repository;

import com.phatbee.cosmeticshopbackend.Entity.ProductFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductFeedbackRepository extends JpaRepository<ProductFeedback, Long> {
    List<ProductFeedback> findByOrderId(Long orderId);
    List<ProductFeedback> findByProductProductId(Long productId);
}