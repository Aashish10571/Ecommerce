package com.ecommerce.backend.product.specification;

import com.ecommerce.backend.product.dto.request.ProductFilterRequestPayload;
import com.ecommerce.backend.product.entity.Product;
import com.ecommerce.backend.product.entity.ProductVariant;
import com.ecommerce.backend.product.enums.ProductStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> build(ProductFilterRequestPayload requestPayload) {
        return ((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            query.distinct(true);

            predicates.add(criteriaBuilder.equal(root.get("status"), ProductStatus.ACTIVE));

            if (requestPayload.categoryId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), requestPayload.categoryId()));
            }

            if (requestPayload.minPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("basePrice"), requestPayload.minPrice()));
            }

            if (requestPayload.maxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("basePrice"), requestPayload.maxPrice()));
            }

            if (requestPayload.keyword() != null && !requestPayload.keyword().isBlank()) {
                String pattern = "%" + requestPayload.keyword().trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern)
                ));
            }

            if (requestPayload.sizeId() != null || requestPayload.colorId() != null || Boolean.TRUE.equals(requestPayload.inStockOnly())) {
                Join<Product, ProductVariant> variants = root.join("variants");

                if (requestPayload.sizeId() != null) {
                    predicates.add(criteriaBuilder.equal(variants.get("size").get("id"), requestPayload.sizeId()));
                }
                if (requestPayload.colorId() != null) {
                    predicates.add(criteriaBuilder.equal(variants.get("color").get("id"), requestPayload.colorId()));
                }
                if (Boolean.TRUE.equals(requestPayload.inStockOnly())) {
                    predicates.add(criteriaBuilder.greaterThan(variants.get("stockQuantity"), 0));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
    }
}
