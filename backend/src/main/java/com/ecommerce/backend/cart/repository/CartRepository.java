package com.ecommerce.backend.cart.repository;

import com.ecommerce.backend.cart.dto.response.CartResponsePayload;
import com.ecommerce.backend.cart.entity.Cart;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findByUser_Id(UUID userId);
}
