package com.example.retry.job;

import com.example.retry.entity.RetryMetadata;
import com.example.retry.entity.Transaction;
import com.example.retry.repository.RetryMetadataRepository;
import com.example.retry.repository.TransactionRepository;
import com.example.retry.service.NotificationService;
import com.example.retry.service.RetryService;
import com.example.retry.strategy.RetryStrategy;
import jakarta.annotation.PostConstruct;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class RetryJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(RetryJob.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private RetryMetadataRepository retryMetadataRepository;

    @Autowired
    private RetryService retryService;

    @Autowired
    private Map<String, RetryStrategy> retryStrategies;

    @Autowired
    private static NotificationService notificationService;

    @Autowired
    public RetryJob(TransactionRepository transactionRepository,
                    RetryMetadataRepository retryMetadataRepository,
                    RetryService retryService,
                    Map<String, RetryStrategy> retryStrategies,
                    NotificationService notificationService) {
        this.transactionRepository = transactionRepository;
        this.retryMetadataRepository = retryMetadataRepository;
        this.retryService = retryService;
        this.retryStrategies = retryStrategies;
        this.notificationService = notificationService;
    }

    @PostConstruct
    public void init() {
        logger.info("RetryJob initialized with dependencies:");
        logger.info("TransactionRepository: {}", transactionRepository);
        logger.info("RetryMetadataRepository: {}", retryMetadataRepository);
        logger.info("RetryService: {}", retryService);
        logger.info("RetryStrategies: {}", retryStrategies);
        logger.info("NotificationService: {}", notificationService);
        if (notificationService == null) {
            logger.error("NotificationService is null in RetryJob");
            throw new IllegalStateException("NotificationService not initialized");
        }
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Long transactionId = dataMap.getLong("transactionId");
        Long metadataId = dataMap.getLong("metadataId");

        MDC.put("correlationId", UUID.randomUUID().toString());

        Transaction transaction = transactionRepository.findById(transactionId).orElse(null);
        RetryMetadata metadata = retryMetadataRepository.findById(metadataId).orElse(null);

        if (transaction == null || metadata == null) {
            logger.error("Transaction or metadata not found for ID: {}", transactionId);
            return;
        }

        RetryStrategy strategy = retryStrategies.get(metadata.getStrategy());
        if (!strategy.shouldRetry(metadata)) {
            transaction.setStatus("FAILED");
            transaction.setLastError("Max retry attempts reached");
            transactionRepository.save(transaction);
            logger.warn("Max retries reached for transaction {}", transactionId);
            return;
        }

        try {
            processTransaction(transaction);
            transaction.setStatus("SUCCESS");
            transactionRepository.save(transaction);
            notificationService.sendNotification("ramigayam1001@gmail.com","Transaction Successful",
                    "Transaction " + transaction.getTransactionId() + " was retried successfully.");
            logger.info("Transaction {} retried successfully", transaction.getTransactionId());
        } catch (Exception e) {
            transaction.setRetryCount(transaction.getRetryCount() + 1);
            metadata.setRetryCount(metadata.getRetryCount() + 1);
            metadata.setLastAttemptTime(System.currentTimeMillis());
            transaction.setLastError(e.getMessage());

            if (metadata.getStrategy().equals("CIRCUIT_BREAKER") && metadata.getRetryCount() >= metadata.getMaxAttempts()) {
                metadata.setCircuitOpen(true);
            }

            transactionRepository.save(transaction);
            retryMetadataRepository.save(metadata);

            try {
                retryService.scheduleRetry(transactionId, metadata.getStrategy(), metadata.getMaxAttempts(), metadata.getIntervalMs());
            } catch (SchedulerException se) {
                logger.error("Failed to reschedule retry for transaction {}", transactionId, se);
            }
            logger.error("Transaction {} failed, scheduling retry. Attempt {}/{}", transactionId, metadata.getRetryCount(), metadata.getMaxAttempts());
        } finally {
            MDC.clear();
        }
    }

    private void processTransaction(Transaction transaction) {
        if (Math.random() > 0.5) {
            throw new RuntimeException("Transient failure");
        }
    }
}