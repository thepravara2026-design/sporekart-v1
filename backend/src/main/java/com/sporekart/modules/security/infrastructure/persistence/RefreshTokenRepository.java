package com.sporekart.modules.security.infrastructure.persistence;

import com.sporekart.modules.security.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    List<RefreshToken> findByTokenFamily(String tokenFamily);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE RefreshToken r SET r.isRevoked = true WHERE r.tokenFamily = :tokenFamily")
    void revokeTokenFamily(@Param("tokenFamily") String tokenFamily);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE RefreshToken r SET r.isRevoked = true WHERE r.userId = :userId")
    void revokeAllUserTokens(@Param("userId") String userId);
}
