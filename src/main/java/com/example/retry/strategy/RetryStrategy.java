package com.example.retry.strategy;

import com.example.retry.entity.RetryMetadata;

public interface RetryStrategy {
    long getNextInterval(RetryMetadata metadata);
    boolean shouldRetry(RetryMetadata metadata);
}