package com.phatbee.cosmeticshopbackend.Service.Impl;

import com.phatbee.cosmeticshopbackend.Entity.Product;
import com.phatbee.cosmeticshopbackend.Repository.ProductRepository;
import com.phatbee.cosmeticshopbackend.dto.ProductSalesDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        product = new Product();
        product.setProductId(1L);
        product.setProductName("Lipstick");
        product.setPrice(20.0);
    }

    // Xem danh sách sản phẩm (3.4.20)
    @Test
    void getAllProducts_withProducts_returnsList() {
        // Arrange
        List<Product> products = Arrays.asList(product);
        when(productRepository.findAll()).thenReturn(products);

        // Act
        List<Product> result = productService.getAllProducts();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Lipstick", result.get(0).getProductName());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void getAllProducts_withNoProducts_returnsEmptyList() {
        // Arrange
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Product> result = productService.getAllProducts();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void getProductsByCategory_withValidCategory_returnsProducts() {
        // Arrange
        List<Product> products = Arrays.asList(product);
        when(productRepository.findByCategoryCategoryId(1L)).thenReturn(products);

        // Act
        List<Product> result = productService.getProductsByCategory(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Lipstick", result.get(0).getProductName());
        verify(productRepository, times(1)).findByCategoryCategoryId(1L);
    }

    @Test
    void getProductsByCategory_withNoProducts_returnsEmptyList() {
        // Arrange
        when(productRepository.findByCategoryCategoryId(1L)).thenReturn(Collections.emptyList());

        // Act
        List<Product> result = productService.getProductsByCategory(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository, times(1)).findByCategoryCategoryId(1L);
    }

    @Test
    void getRecentProducts_withRecentProducts_returnsLimitedList() {
        // Arrange
        List<Product> products = Arrays.asList(product, new Product(), new Product());
        when(productRepository.findRecentProducts(any(LocalDate.class))).thenReturn(products);

        // Act
        List<Product> result = productService.getRecentProducts();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(productRepository, times(1)).findRecentProducts(any(LocalDate.class));
    }

    @Test
    void getRecentProducts_withNoRecentProducts_returnsEmptyList() {
        // Arrange
        when(productRepository.findRecentProducts(any(LocalDate.class))).thenReturn(Collections.emptyList());

        // Act
        List<Product> result = productService.getRecentProducts();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository, times(1)).findRecentProducts(any(LocalDate.class));
    }

    @Test
    void getTopSellingProducts_withSales_returnsLimitedList() {
        // Arrange
        Object[] result = new Object[]{product, 100L};
        List<Object[]> results = Arrays.asList(new Object[][]{result});
        when(productRepository.findTopSellingProducts()).thenReturn(results);

        // Act
        List<ProductSalesDTO> resultList = productService.getTopSellingProducts();

        // Assert
        assertNotNull(resultList);
        assertEquals(1, resultList.size());
        assertEquals("Lipstick", resultList.get(0).getProduct().getProductName());
        assertEquals(100L, resultList.get(0).getTotalQuantity());
        verify(productRepository, times(1)).findTopSellingProducts();
    }

    @Test
    void getTopSellingProducts_withNoSales_returnsEmptyList() {
        // Arrange
        List<Object[]> emptyResults = Collections.emptyList();
        when(productRepository.findTopSellingProducts()).thenReturn(emptyResults);

        // Act
        List<ProductSalesDTO> result = productService.getTopSellingProducts();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository, times(1)).findTopSellingProducts();
    }

    // Xem chi tiết sản phẩm
    @Test
    void getProductById_withValidId_returnsProduct() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act
        Product result = productService.getProductById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Lipstick", result.getProductName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getProductById_withInvalidId_returnsNull() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Product result = productService.getProductById(999L);

        // Assert
        assertNull(result);
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    void findById_withValidId_returnsProduct() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Act
        Product result = productService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Lipstick", result.getProductName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void findById_withInvalidId_returnsNull() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Product result = productService.findById(999L);

        // Assert
        assertNull(result);
        verify(productRepository, times(1)).findById(999L);
    }

    // Tìm kiếm sản phẩm (3.4.21)
    @Test
    void searchProducts_withValidKeyword_returnsProducts() {
        // Arrange
        List<Product> products = Arrays.asList(product);
        when(productRepository.searchProducts("lipstick")).thenReturn(products);

        // Act
        List<Product> result = productService.searchProducts("lipstick");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Lipstick", result.get(0).getProductName());
        verify(productRepository, times(1)).searchProducts("lipstick");
    }

    @Test
    void searchProducts_withEmptyKeyword_returnsAllProducts() {
        // Arrange
        List<Product> products = Arrays.asList(product);
        when(productRepository.findAll()).thenReturn(products);

        // Act
        List<Product> result = productService.searchProducts("");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Lipstick", result.get(0).getProductName());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void searchProducts_withNullKeyword_returnsAllProducts() {
        // Arrange
        List<Product> products = Arrays.asList(product);
        when(productRepository.findAll()).thenReturn(products);

        // Act
        List<Product> result = productService.searchProducts(null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Lipstick", result.get(0).getProductName());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void searchProducts_withNoMatchingProducts_returnsEmptyList() {
        // Arrange
        when(productRepository.searchProducts("invalid")).thenReturn(Collections.emptyList());

        // Act
        List<Product> result = productService.searchProducts("invalid");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository, times(1)).searchProducts("invalid");
    }

    @Test
    void getProductsByIds_withValidIds_returnsProducts() {
        // Arrange
        List<Long> productIds = Arrays.asList(1L);
        List<Product> products = Arrays.asList(product);
        when(productRepository.findAllById(productIds)).thenReturn(products);

        // Act
        List<Product> result = productService.getProductsByIds(productIds);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Lipstick", result.get(0).getProductName());
        verify(productRepository, times(1)).findAllById(productIds);
    }

    @Test
    void getProductsByIds_withEmptyIds_returnsEmptyList() {
        // Arrange
        List<Long> productIds = Collections.emptyList();
        when(productRepository.findAllById(productIds)).thenReturn(Collections.emptyList());

        // Act
        List<Product> result = productService.getProductsByIds(productIds);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository, times(1)).findAllById(productIds);
    }
}