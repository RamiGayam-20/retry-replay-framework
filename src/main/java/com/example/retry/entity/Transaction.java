package com.example.retry.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String transactionId;
    private String systemId;
    private String transactionType;
    private String status; // PENDING, SUCCESS, FAILED
    private String payload;
    private int retryCount;
    private String lastError;
    private boolean isStateful;
}