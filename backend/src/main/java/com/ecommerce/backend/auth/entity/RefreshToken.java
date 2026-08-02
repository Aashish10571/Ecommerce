package com.ecommerce.backend.auth.entity;

import com.ecommerce.backend.common.audit.AuditTable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "refresh_token")
public class RefreshToken extends AuditTable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "token_id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "revoked", nullable = false)
    private boolean revoked;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
