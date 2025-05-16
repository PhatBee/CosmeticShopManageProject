package com.phatbee.cosmeticshopbackend.Service.Impl;

import com.phatbee.cosmeticshopbackend.Entity.Product;
import com.phatbee.cosmeticshopbackend.Entity.ProductFeedback;
import com.phatbee.cosmeticshopbackend.Repository.ProductFeedbackRepository;
import com.phatbee.cosmeticshopbackend.Repository.ProductRepository;
import com.phatbee.cosmeticshopbackend.Service.ProductFeedbackService;
import com.phatbee.cosmeticshopbackend.dto.ProductFeedbackDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductFeedbackServiceImpl implements ProductFeedbackService {
    @Autowired
    private ProductFeedbackRepository feedbackRepository; //

    @Autowired
    private ProductRepository productRepository;

    @Override
    public ProductFeedbackDTO createFeedback(ProductFeedbackDTO feedbackDTO) {
        ProductFeedback feedback = new ProductFeedback();
        feedback.setComment(feedbackDTO.getComment());
        feedback.setImage(feedbackDTO.getImage());
        feedback.setFeedbackDate(LocalDateTime.now());
        feedback.setCustomerId(feedbackDTO.getCustomerId());
        feedback.setOrderId(feedbackDTO.getOrderId());
        feedback.setProductSnapshotName(feedbackDTO.getProductSnapshotName());
        feedback.setRating(feedbackDTO.getRating());

        Product product = productRepository.findById(feedbackDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        feedback.setProduct(product);

        feedback = feedbackRepository.save(feedback);
        return new ProductFeedbackDTO(feedback);    }

    @Override
    public ProductFeedbackDTO updateFeedback(Long feedbackId, ProductFeedbackDTO feedbackDTO) {
        ProductFeedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));
        feedback.setComment(feedbackDTO.getComment());
        feedback.setImage(feedbackDTO.getImage());
        feedback.setRating(feedbackDTO.getRating());
        feedback.setProductSnapshotName(feedbackDTO.getProductSnapshotName());

        feedback = feedbackRepository.save(feedback);
        return new ProductFeedbackDTO(feedback);
    }

    @Override
    public List<ProductFeedbackDTO> getFeedbackByOrderId(Long orderId) {
        List<ProductFeedback> feedbackList = feedbackRepository.findByOrderId(orderId);
        return feedbackList.stream().map(ProductFeedbackDTO::new).collect(Collectors.toList());    }

    @Override
    public List<ProductFeedback> findByProductProductId(Long productId) {
        return feedbackRepository.findByProductProductId(productId);
    }
}
