package com.phatbee.cosmeticshopbackend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.phatbee.cosmeticshopbackend.Entity.ProductFeedback;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ProductFeedbackDTO implements Serializable {
    private Long productFeedbackId;
    private String comment;
    private String image;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime feedbackDate;

    private Long customerId;
    private Long orderId;
    private String productSnapshotName;
    private Double rating;
    private Long productId;

    public ProductFeedbackDTO() {
        // constructor mặc định cần thiết cho Jackson
    }


    public ProductFeedbackDTO(ProductFeedback feedback) {
        this.productFeedbackId = feedback.getProductFeedbackId();
        this.comment = feedback.getComment();
        this.image = feedback.getImage();
        this.feedbackDate = feedback.getFeedbackDate();
        this.customerId = feedback.getCustomerId();
        this.orderId = feedback.getOrderId();
        this.productSnapshotName = feedback.getProductSnapshotName();
        this.rating = feedback.getRating();
        this.productId = feedback.getProduct() != null ? feedback.getProduct().getProductId() : null;
    }
}