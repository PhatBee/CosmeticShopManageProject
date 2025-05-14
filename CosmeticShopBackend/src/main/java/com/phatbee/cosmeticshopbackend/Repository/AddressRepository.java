package com.phatbee.cosmeticshopbackend.Repository;

import com.phatbee.cosmeticshopbackend.Entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByCustomerUserId (Long userId);

    @Query("SELECT a FROM Address a WHERE a.defaultAddress = true AND a.customer.userId = :userId")
    List<Address> findDefaultAddresses(@Param("userId") Long userId);
}
