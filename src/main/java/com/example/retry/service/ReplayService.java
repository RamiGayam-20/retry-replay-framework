package com.example.retry.service;

import com.example.retry.entity.Transaction;
import com.example.retry.job.ReplayJob;
import com.example.retry.repository.TransactionRepository;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class ReplayService {
    private static final Logger logger = LoggerFactory.getLogger(ReplayService.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private Scheduler scheduler;
    @Autowired
    private NotificationService notificationService;

    @Value("${app.notification.recipient}")
    private String notificationRecipient;

    public void replayTransaction(Long transactionId, boolean immediate) throws SchedulerException {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        if (!isValidForReplay(transaction)) {
            throw new IllegalStateException("Transaction cannot be replayed");
        }

        if (immediate) {
            processReplay(transaction);
        } else {
            scheduleReplay(transaction);
        }
    }

    private boolean isValidForReplay(Transaction transaction) {
        return transaction.getStatus().equals("FAILED");
    }

    private void processReplay(Transaction transaction) {
        MDC.put("correlationId", UUID.randomUUID().toString());
        try {
            transaction.setStatus("SUCCESS");
            transaction.setRetryCount(0);
            transaction.setLastError(null);
            transactionRepository.save(transaction);
            notificationService.sendNotification(notificationRecipient,"Transaction Successful",
                    "Transaction " + transaction.getTransactionId() + " Replayed successfully.");
            logger.info("Transaction {} replayed successfully", transaction.getTransactionId());
        } catch (Exception e) {
            transaction.setLastError(e.getMessage());
            transactionRepository.save(transaction);
            logger.error("Replay failed for transaction {}", transaction.getId(), e);
        } finally {
            MDC.clear();
        }
    }

    private void scheduleReplay(Transaction transaction) throws SchedulerException {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("transactionId", transaction.getId());

        JobDetail job = JobBuilder.newJob(ReplayJob.class)
                .withIdentity("replayJob-" + transaction.getId(), "replayGroup")
                .usingJobData(jobDataMap)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("replayTrigger-" + transaction.getId(), "replayGroup")
                .startAt(new Date(System.currentTimeMillis() + 1000))
                .build();

        scheduler.scheduleJob(job, trigger);
        logger.info("Scheduled replay for transaction {}", transaction.getId());
    }
}