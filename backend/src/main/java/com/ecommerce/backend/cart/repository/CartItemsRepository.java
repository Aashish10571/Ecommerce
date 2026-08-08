package com.ecommerce.backend.cart.repository;

import com.ecommerce.backend.cart.entity.CartItems;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemsRepository extends JpaRepository<CartItems, UUID> {
    Page<CartItems> findByCart_User_Id(UUID cartUserId, Pageable pageable);

    Optional<CartItems> findByCart_IdAndVariant_Id(UUID cartId, UUID variantId);

    Optional<CartItems> findByIdAndCart_User_Id(UUID itemId, UUID userId);
}
