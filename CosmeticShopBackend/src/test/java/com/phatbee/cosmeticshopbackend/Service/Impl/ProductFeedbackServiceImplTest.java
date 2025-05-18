package com.phatbee.cosmeticshopbackend.Service.Impl;

import com.phatbee.cosmeticshopbackend.dto.ProductFeedbackDTO;
import com.phatbee.cosmeticshopbackend.Entity.Product;
import com.phatbee.cosmeticshopbackend.Entity.ProductFeedback;
import com.phatbee.cosmeticshopbackend.Repository.ProductFeedbackRepository;
import com.phatbee.cosmeticshopbackend.Repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductFeedbackServiceImplTest {

    @Mock
    private ProductFeedbackRepository feedbackRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductFeedbackServiceImpl feedbackService;

    private Product product;
    private ProductFeedback feedback;
    private ProductFeedbackDTO feedbackDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        product = new Product();
        product.setProductId(1L);
        product.setProductName("Lipstick");

        feedback = new ProductFeedback();
        feedback.setProductFeedbackId(1L);
        feedback.setComment("Great product!");
        feedback.setRating(5.0);
        feedback.setProduct(product);
        feedback.setCustomerId(1L);
        feedback.setOrderId(1L);
        feedback.setFeedbackDate(LocalDateTime.now());

        feedbackDTO = new ProductFeedbackDTO();
        feedbackDTO.setProductId(1L);
        feedbackDTO.setComment("Great product!");
        feedbackDTO.setRating(5.0);
        feedbackDTO.setCustomerId(1L);
        feedbackDTO.setOrderId(1L);
    }

    // Đánh giá sản phẩm
    @Test
    void createFeedback_withValidDTO_createsFeedback() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(feedbackRepository.save(any(ProductFeedback.class))).thenReturn(feedback);

        // Act
        ProductFeedbackDTO result = feedbackService.createFeedback(feedbackDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Great product!", result.getComment());
        assertEquals(5, result.getRating());
        verify(productRepository, times(1)).findById(1L);
        verify(feedbackRepository, times(1)).save(any(ProductFeedback.class));
    }

    @Test
    void createFeedback_withNonExistentProduct_throwsException() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> feedbackService.createFeedback(feedbackDTO), "Product not found");
        verify(productRepository, times(1)).findById(1L);
        verify(feedbackRepository, never()).save(any(ProductFeedback.class));
    }

    @Test
    void updateFeedback_withValidId_updatesFeedback() {
        // Arrange
        ProductFeedbackDTO updateDTO = new ProductFeedbackDTO();
        updateDTO.setComment("Updated comment");
        updateDTO.setRating(4.0);
        when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedback));
        when(feedbackRepository.save(any(ProductFeedback.class))).thenReturn(feedback);

        // Act
        ProductFeedbackDTO result = feedbackService.updateFeedback(1L, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Updated comment", result.getComment());
        assertEquals(4, result.getRating());
        verify(feedbackRepository, times(1)).findById(1L);
        verify(feedbackRepository, times(1)).save(any(ProductFeedback.class));
    }

    @Test
    void updateFeedback_withNonExistentId_throwsException() {
        // Arrange
        when(feedbackRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> feedbackService.updateFeedback(999L, feedbackDTO), "Feedback not found");
        verify(feedbackRepository, times(1)).findById(999L);
        verify(feedbackRepository, never()).save(any(ProductFeedback.class));
    }

    @Test
    void getFeedbackByOrderId_withValidOrderId_returnsFeedbackList() {
        // Arrange
        List<ProductFeedback> feedbackList = Arrays.asList(feedback);
        when(feedbackRepository.findByOrderId(1L)).thenReturn(feedbackList);

        // Act
        List<ProductFeedbackDTO> result = feedbackService.getFeedbackByOrderId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Great product!", result.get(0).getComment());
        verify(feedbackRepository, times(1)).findByOrderId(1L);
    }

    @Test
    void getFeedbackByOrderId_withNoFeedback_returnsEmptyList() {
        // Arrange
        when(feedbackRepository.findByOrderId(1L)).thenReturn(Collections.emptyList());

        // Act
        List<ProductFeedbackDTO> result = feedbackService.getFeedbackByOrderId(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(feedbackRepository, times(1)).findByOrderId(1L);
    }

    // Xem chi tiết sản phẩm (đánh giá)
    @Test
    void findByProductProductId_withValidProductId_returnsFeedbackList() {
        // Arrange
        List<ProductFeedback> feedbackList = Arrays.asList(feedback);
        when(feedbackRepository.findByProductProductId(1L)).thenReturn(feedbackList);

        // Act
        List<ProductFeedback> result = feedbackService.findByProductProductId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Great product!", result.get(0).getComment());
        verify(feedbackRepository, times(1)).findByProductProductId(1L);
    }

    @Test
    void findByProductProductId_withNoFeedback_returnsEmptyList() {
        // Arrange
        when(feedbackRepository.findByProductProductId(1L)).thenReturn(Collections.emptyList());

        // Act
        List<ProductFeedback> result = feedbackService.findByProductProductId(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(feedbackRepository, times(1)).findByProductProductId(1L);
    }
}