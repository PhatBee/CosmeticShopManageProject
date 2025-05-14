package com.phatbee.cosmeticshopbackend.Repository;

import com.phatbee.cosmeticshopbackend.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByUserId(Long userId);

    Optional<User> findByEmail(String email);
    Optional<User> findByOtp(String otp);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
