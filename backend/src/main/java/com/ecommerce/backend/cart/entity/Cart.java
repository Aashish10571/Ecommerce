package com.ecommerce.backend.cart.entity;

import com.ecommerce.backend.auth.entity.User;
import com.ecommerce.backend.common.audit.AuditTable;
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
@Table(name = "cart")
public class Cart extends AuditTable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "cart_id", nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder.Default
    @OneToMany(
            mappedBy = "cart",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<CartItems> items = new ArrayList<>();
}
