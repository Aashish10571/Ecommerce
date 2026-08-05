package com.ecommerce.backend.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_size")
public class ProductSize {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "size_id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "label",nullable = false, unique = true, length = 20)
    private String label;

    @Column(name = "sort_order",nullable = false)
    private Integer sortOrder = 0;

    @OneToMany(mappedBy = "size")
    private List<ProductVariant> variants = new ArrayList<>();
}
