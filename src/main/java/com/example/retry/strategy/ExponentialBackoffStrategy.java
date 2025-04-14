package com.example.retry.strategy;

import com.example.retry.entity.RetryMetadata;
import org.springframework.stereotype.Component;

@Component("EXPONENTIAL")
public class ExponentialBackoffStrategy implements RetryStrategy {
    @Override
    public long getNextInterval(RetryMetadata metadata) {
        return metadata.getIntervalMs() * (long) Math.pow(2, metadata.getRetryCount());
    }

    @Override
    public boolean shouldRetry(RetryMetadata metadata) {
        return metadata.getRetryCount() < metadata.getMaxAttempts();
    }
}