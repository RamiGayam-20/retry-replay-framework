package com.example.retry.strategy;

import com.example.retry.entity.RetryMetadata;
import org.springframework.stereotype.Component;

@Component("CIRCUIT_BREAKER")
public class CircuitBreakerStrategy implements RetryStrategy {
    private static final long RESET_TIMEOUT_MS = 60000;

    @Override
    public long getNextInterval(RetryMetadata metadata) {
        return metadata.getIntervalMs();
    }

    @Override
    public boolean shouldRetry(RetryMetadata metadata) {
        if (metadata.isCircuitOpen()) {
            long timeSinceLastAttempt = System.currentTimeMillis() - metadata.getLastAttemptTime();
            if (timeSinceLastAttempt > RESET_TIMEOUT_MS) {
                metadata.setCircuitOpen(false);
                return true;
            }
            return false;
        }
        return metadata.getRetryCount() < metadata.getMaxAttempts();
    }
}