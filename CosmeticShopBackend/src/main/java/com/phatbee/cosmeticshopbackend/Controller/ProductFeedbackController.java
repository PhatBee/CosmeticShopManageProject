package com.phatbee.cosmeticshopbackend.Controller;

import com.phatbee.cosmeticshopbackend.Entity.ProductFeedback;
import com.phatbee.cosmeticshopbackend.Service.ProductFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/product-feedbacks")

public class ProductFeedbackController {
    @Autowired
    private ProductFeedbackService feedbackRepository;

    @GetMapping("/product/{productId}")
    public List<ProductFeedback> getFeedbackByProductId(@PathVariable Long productId) {
        return feedbackRepository.findByProductProductId(productId);
    }
}
