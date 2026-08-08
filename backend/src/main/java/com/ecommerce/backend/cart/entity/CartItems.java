package com.ecommerce.backend.cart.entity;

import com.ecommerce.backend.common.audit.AuditTable;
import com.ecommerce.backend.product.entity.ProductVariant;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "cart_items",
        uniqueConstraints = @UniqueConstraint(columnNames = { "cart_id", "variant_id" })
)
public class CartItems extends AuditTable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "item_id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;
}
