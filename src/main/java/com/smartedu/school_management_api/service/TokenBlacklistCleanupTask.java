package com.smartedu.school_management_api.service;

import com.smartedu.school_management_api.repository.TokenBlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Prunes the token blacklist. Once a revoked token is past its own expiry it fails
 * validation regardless, so keeping the row buys nothing but table growth.
 */
@Component
@RequiredArgsConstructor
public class TokenBlacklistCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistCleanupTask.class);

    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Scheduled(cron = "${app.security.token-cleanup-cron:0 0 3 * * *}")
    @Transactional
    public void purgeExpiredTokens() {
        int removed = tokenBlacklistRepository.deleteExpiredBefore(Instant.now());
        if (removed > 0) {
            log.info("Purged {} expired blacklisted token(s)", removed);
        }
    }
}
