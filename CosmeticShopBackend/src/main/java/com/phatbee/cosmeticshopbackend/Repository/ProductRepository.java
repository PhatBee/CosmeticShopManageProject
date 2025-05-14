package com.phatbee.cosmeticshopbackend.Repository;

import com.phatbee.cosmeticshopbackend.Entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryCategoryId(Long categoryId);
    Optional<Product> findByProductId(Long productId);

    @Query("SELECT p FROM Product p WHERE p.manufactureDate >= :startDate ORDER BY p.manufactureDate desc ")
    List<Product> findRecentProducts (@Param("startDate") LocalDate startDate);
    @Query("SELECT ol.product, SUM(ol.quantity) AS totalQuantity FROM OrderLine ol " +
            "GROUP BY ol.product " +
            "ORDER BY totalQuantity DESC")
    List<Object[]> findTopSellingProducts();

    @Query("SELECT p FROM Product p WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchProducts(@Param("keyword") String keyword);
}
