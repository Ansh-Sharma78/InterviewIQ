package com.interviewiq.auth.repository;

import com.interviewiq.auth.entity.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update RefreshToken rt set rt.revoked = true, rt.revokedAt = CURRENT_TIMESTAMP where rt.user.id = :userId and rt.revoked = false")
    int revokeAllActiveByUserId(Long userId);
}

