package com.phatbee.cosmeticshopbackend.Service.Impl;

import com.phatbee.cosmeticshopbackend.Entity.Category;
import com.phatbee.cosmeticshopbackend.Repository.CategoryRepository;
import com.phatbee.cosmeticshopbackend.dto.CategoryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        category = new Category();
        category.setCategoryId(1);
        category.setCategoryName("Skincare");
        category.setImageUrl("skincare.jpg");
    }

    @Test
    void getAllCategories_withCategories_returnsList() {
        // Arrange
        List<Category> categories = Arrays.asList(category);
        when(categoryRepository.findAllByOrderByCategoryNameAsc()).thenReturn(categories);

        // Act
        List<CategoryDTO> result = categoryService.getAllCategories();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Skincare", result.get(0).getCategoryName());
        assertEquals("skincare.jpg", result.get(0).getImageUrl());
        assertEquals(1, result.get(0).getCategoryId());
        verify(categoryRepository, times(1)).findAllByOrderByCategoryNameAsc();
    }

    @Test
    void getAllCategories_withNoCategories_returnsEmptyList() {
        // Arrange
        when(categoryRepository.findAllByOrderByCategoryNameAsc()).thenReturn(Collections.emptyList());

        // Act
        List<CategoryDTO> result = categoryService.getAllCategories();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(categoryRepository, times(1)).findAllByOrderByCategoryNameAsc();
    }
}