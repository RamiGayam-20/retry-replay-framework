package com.example.retry.service;

import com.example.retry.entity.RetryMetadata;
import com.example.retry.entity.Transaction;
import com.example.retry.job.RetryJob;
import com.example.retry.repository.RetryMetadataRepository;
import com.example.retry.repository.TransactionRepository;
import com.example.retry.strategy.RetryStrategy;
import jakarta.annotation.PostConstruct;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class RetryService {
    private static final Logger logger = LoggerFactory.getLogger(RetryService.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private RetryMetadataRepository retryMetadataRepository;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private Map<String, RetryStrategy> retryStrategies;

    @PostConstruct
    public void init() {
        logger.info("RetryService initialized with scheduler: {}", scheduler);
    }

    public void scheduleRetry(Long transactionId, String strategy, int maxAttempts, long intervalMs) throws SchedulerException {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
        if (transaction.getStatus().equals("SUCCESS")) {
            logger.error("Cannot schedule retry for successful transaction {}", transactionId);
            throw new IllegalStateException("Cannot retry a successful transaction");
        }

        RetryMetadata metadata = new RetryMetadata();
        metadata.setTransactionId(transactionId);
        metadata.setStrategy(strategy);
        metadata.setMaxAttempts(maxAttempts);
        metadata.setIntervalMs(intervalMs);
        metadata.setRetryCount(0);
        retryMetadataRepository.save(metadata);

        scheduleJob(transaction, metadata);
    }
    private void scheduleJob(Transaction transaction, RetryMetadata metadata) throws SchedulerException {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("transactionId", transaction.getId());
        jobDataMap.put("metadataId", metadata.getId());

        JobDetail job = JobBuilder.newJob(RetryJob.class)
                .withIdentity("retryJob-" + transaction.getId(), "retryGroup")
                .usingJobData(jobDataMap)
                .build();

        RetryStrategy strategy = retryStrategies.get(metadata.getStrategy());
        long interval = strategy.getNextInterval(metadata);

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("retryTrigger-" + transaction.getId(), "retryGroup")
                .startAt(new Date(System.currentTimeMillis() + interval))
                .build();

        scheduler.scheduleJob(job, trigger);
        logger.info("Scheduled retry for transaction {} with strategy {}, interval {}ms",
                transaction.getId(), metadata.getStrategy(), interval);
    }
   }