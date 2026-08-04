package com.ecommerce.backend.category.repository;

import com.ecommerce.backend.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Category findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsByName(String name);
}
