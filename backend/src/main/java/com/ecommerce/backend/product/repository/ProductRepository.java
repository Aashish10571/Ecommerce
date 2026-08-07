package com.ecommerce.backend.product.repository;

import com.ecommerce.backend.category.entity.Category;
import com.ecommerce.backend.product.entity.Product;
import com.ecommerce.backend.product.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    Optional<Product> findBySlug(String slug);

    Page<Product> findAllByStatus(ProductStatus status, Pageable pageable);

    @Modifying
    @Query("UPDATE Product p SET p.category = :newCategory WHERE p.category = :oldCategory")
    void reassignCategory(@Param("oldCategory") Category oldCategory, @Param("newCategory") Category newCategory);
}