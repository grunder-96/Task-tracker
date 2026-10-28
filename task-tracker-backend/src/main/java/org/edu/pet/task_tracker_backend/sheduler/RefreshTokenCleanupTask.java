package org.edu.pet.task_tracker_backend.sheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu.pet.task_tracker_backend.repository.RefreshTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenCleanupTask {

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(
            timeUnit = TimeUnit.HOURS,
            initialDelayString = "${app.cleanup-tokens.initial-delay-hours}",
            fixedRateString = "${app.cleanup-tokens.fixed-rate-hours}")
    @Transactional
    public void cleanupExpiredRefreshTokens() {

        int deleted = refreshTokenRepository.deleteByExpiresAtBefore(Instant.now());
        log.info("Deleted {} expired refresh tokens", deleted);
    }
}