package com.phatbee.cosmeticshopbackend.Service.Impl;

import com.phatbee.cosmeticshopbackend.Entity.Category;
import com.phatbee.cosmeticshopbackend.Repository.CategoryRepository;
import com.phatbee.cosmeticshopbackend.Service.CategoryService;
import com.phatbee.cosmeticshopbackend.dto.CategoryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<CategoryDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAllByOrderByCategoryNameAsc();
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private CategoryDTO convertToDTO(Category category) {
        return new CategoryDTO(
                Math.toIntExact(category.getCategoryId()),
                category.getCategoryName(),
                category.getImageUrl()
        );
    }
}
