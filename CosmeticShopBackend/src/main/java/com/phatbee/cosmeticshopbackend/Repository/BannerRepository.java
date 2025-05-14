package com.phatbee.cosmeticshopbackend.Repository;

import com.phatbee.cosmeticshopbackend.Entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Integer> {
    @Query("SELECT b FROM Banner b WHERE b.active = true ORDER BY b.displayOrder ASC")
    List<Banner> findActiveOrderByDisplayOrder();

}
