package com.ecommerce.backend.product.repository;

import com.ecommerce.backend.product.entity.ProductSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductSizeRepository extends JpaRepository<ProductSize, UUID> {
    List<ProductSize> findAllByOrderBySortOrderAsc();

    Optional<ProductSize> findByLabel(String label);
}
