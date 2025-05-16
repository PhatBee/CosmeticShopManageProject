package vn.phatbee.cosmesticshopapp.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ProductFeedback implements Serializable {
    @SerializedName("productFeedbackId")
    private Long productFeedbackId;

    @SerializedName("comment")
    private String comment;

    @SerializedName("image")
    private String image;

    @SerializedName("feedbackDate")
    private LocalDateTime feedbackDate;

    @SerializedName("customerId")
    private Long customerId;

    @SerializedName("orderId")
    private int orderId;

    @SerializedName("productSnapshotName")
    private String productSnapshotName;

    @SerializedName("rating")
    private Double rating;

    @SerializedName("productId")
    private Long productId;

    // Getters and Setters
    public Long getProductFeedbackId() { return productFeedbackId; }
    public void setProductFeedbackId(Long productFeedbackId) { this.productFeedbackId = productFeedbackId; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public LocalDateTime getFeedbackDate() { return feedbackDate; }
    public void setFeedbackDate(LocalDateTime feedbackDate) { this.feedbackDate = feedbackDate; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public String getProductSnapshotName() { return productSnapshotName; }
    public void setProductSnapshotName(String productSnapshotName) { this.productSnapshotName = productSnapshotName; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
}