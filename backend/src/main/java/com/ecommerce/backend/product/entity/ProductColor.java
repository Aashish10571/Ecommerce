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
@Table(name = "product_color")
public class ProductColor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "color_id", nullable = false, updatable = false)
    private UUID id;


    @Column(name = "name",nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "code",length = 7)
    private String hexCode;

    @OneToMany(mappedBy = "color")
    private List<ProductVariant> variants = new ArrayList<>();
}
