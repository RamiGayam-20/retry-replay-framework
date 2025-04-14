package com.example.retry;

import com.example.retry.entity.Transaction;
import com.example.retry.repository.TransactionRepository;
import com.example.retry.service.RetryService;
import org.junit.jupiter.api.Test;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class RetryServiceTest {
    @Autowired
    private RetryService retryService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    public void testScheduleRetry() throws SchedulerException {
        Transaction transaction = new Transaction();
        transaction.setTransactionId("TXN123");
        transaction.setStatus("FAILED");
        transaction = transactionRepository.save(transaction);

        retryService.scheduleRetry(transaction.getId(), "FIXED", 3, 1000);

        Transaction updated = transactionRepository.findById(transaction.getId()).get();
        assertEquals("FAILED", updated.getStatus());
    }
}