package com.phatbee.cosmeticshopbackend.Service;

import com.phatbee.cosmeticshopbackend.Entity.ProductFeedback;
import com.phatbee.cosmeticshopbackend.dto.ProductFeedbackDTO;

import java.util.List;

public interface ProductFeedbackService {
    ProductFeedbackDTO createFeedback(ProductFeedbackDTO feedbackDTO);
    ProductFeedbackDTO updateFeedback(Long feedbackId, ProductFeedbackDTO feedbackDTO);
    List<ProductFeedbackDTO> getFeedbackByOrderId(Long orderId);
    List<ProductFeedback> findByProductProductId(Long productId);
}
