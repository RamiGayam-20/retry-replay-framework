package com.example.retry.strategy;

import com.example.retry.entity.RetryMetadata;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component("JITTER")
public class JitterStrategy implements RetryStrategy {
    private static final double JITTER_FACTOR = 0.1;

    @Override
    public long getNextInterval(RetryMetadata metadata) {
        long baseInterval = metadata.getIntervalMs();
        Random random = new Random();
        long jitter = (long) (baseInterval * JITTER_FACTOR * random.nextGaussian());
        return baseInterval + jitter;
    }

    @Override
    public boolean shouldRetry(RetryMetadata metadata) {
        return metadata.getRetryCount() < metadata.getMaxAttempts();
    }
}