package com.smartedu.school_management_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Revoked JWTs. Rows are pruned once past {@code expiresAt} — after that the
 * token fails signature/expiry validation on its own.
 *
 * <p>Mapped to {@code revoked_tokens} rather than the old {@code token_blacklist}:
 * that table stores the raw token in a {@code NOT NULL UNIQUE} column this entity
 * no longer writes, so inserts into it would fail. The legacy table can be dropped
 * once deployed — it only ever held already-expired sessions.
 */
@Entity
@Table(name = "revoked_tokens", indexes = {
        @Index(name = "idx_revoked_tokens_expires", columnList = "expires_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** SHA-256 hash of the token, so raw credentials are never stored at rest. */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at", nullable = false)
    @Builder.Default
    private Instant revokedAt = Instant.now();
}
