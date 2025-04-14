package com.example.retry;

import com.example.retry.entity.Transaction;
import com.example.retry.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RetryIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testRetryTransaction() throws Exception {
        Transaction transaction = new Transaction();
        transaction.setTransactionId("TXN123");
        transaction.setStatus("FAILED");
        transaction = transactionRepository.save(transaction);

        mockMvc.perform(post("/transactions/retry/" + transaction.getId())
                        .param("strategy", "FIXED"))
                .andExpect(status().is3xxRedirection());

        Thread.sleep(2000);
        Transaction updated = transactionRepository.findById(transaction.getId()).get();
        assertTrue(updated.getRetryCount() > 0 || updated.getStatus().equals("SUCCESS"));
    }
}