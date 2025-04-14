package com.example.retry.strategy;

import com.example.retry.entity.RetryMetadata;
import org.springframework.stereotype.Component;

@Component("FIXED")
public class FixedIntervalStrategy implements RetryStrategy {
    @Override
    public long getNextInterval(RetryMetadata metadata) {
        return metadata.getIntervalMs();
    }

    @Override
    public boolean shouldRetry(RetryMetadata metadata) {
        return metadata.getRetryCount() < metadata.getMaxAttempts();
    }
}