package com.example.retry.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class RetryMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long transactionId;
    private String strategy; // FIXED, EXPONENTIAL, CIRCUIT_BREAKER, JITTER
    private int maxAttempts;
    private long intervalMs;
    private boolean isCircuitOpen;
    private long lastAttemptTime;
    private int retryCount;
}