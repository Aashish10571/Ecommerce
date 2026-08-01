package com.ecommerce.backend.auth.repository;

import com.ecommerce.backend.auth.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, UUID> {
    void deleteByEmailAndUsed(String email, boolean used);

    Optional<VerificationCode> findByEmailAndCode(String email, String code);
}
