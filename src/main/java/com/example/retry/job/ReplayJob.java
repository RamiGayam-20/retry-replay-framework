package com.example.retry.job;

import com.example.retry.service.ReplayService;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReplayJob implements Job {
    @Autowired
    private ReplayService replayService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Long transactionId = dataMap.getLong("transactionId");
        try {
            replayService.replayTransaction(transactionId, true);
        } catch (SchedulerException e) {
            throw new JobExecutionException("Failed to execute replay", e);
        }
    }
}