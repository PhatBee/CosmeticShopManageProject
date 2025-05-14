package com.phatbee.cosmeticshopbackend.Service;

import com.phatbee.cosmeticshopbackend.Entity.Product;
import com.phatbee.cosmeticshopbackend.dto.ProductSalesDTO;

import java.util.List;

public interface ProductService {
    List<Product> getAllProducts();
    List<Product> getProductsByCategory(Long categoryID);
    List<Product> getRecentProducts();
    List<ProductSalesDTO> getTopSellingProducts();
    Product findById(Long productId);
    Product getProductById(Long productID);
    List<Product> searchProducts(String keyword);
    List<Product> getProductsByIds(List<Long> productIds);
}
