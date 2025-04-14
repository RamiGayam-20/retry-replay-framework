package com.example.retry.controller;

import com.example.retry.repository.TransactionRepository;
import com.example.retry.service.ReplayService;
import com.example.retry.service.RetryService;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/transactions")
public class TransactionController {
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private RetryService retryService;

    @Autowired
    private ReplayService replayService;

    @GetMapping("/view")
    public String viewTransactions(Model model) {
        model.addAttribute("transactions", transactionRepository.findAll());
        return "transactions";
    }

    @GetMapping
    public String listTransactions(Model model) {
        model.addAttribute("transactions", transactionRepository.findAll());
        return "transactions";
    }

    @PostMapping("/retry/{id}")
    public String retryTransaction(@PathVariable Long id, @RequestParam String strategy, Model model) {
        try {
            retryService.scheduleRetry(id, strategy, 3, 1000);
            listTransactions(model);
            return "redirect:/transactions";
        }catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("transactions", transactionRepository.findAll());
            return "transactions";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to schedule retry: " + e.getMessage());
            model.addAttribute("transactions", transactionRepository.findAll());
            return "transactions";
        }
    }

    @PostMapping("/replay/{id}")
    public String replayTransaction(@PathVariable Long id, @RequestParam boolean immediate, Model model) {
        try {
            replayService.replayTransaction(id, immediate);
            listTransactions(model);
            return "redirect:/transactions";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to replay transaction: " + e.getMessage());
            model.addAttribute("transactions", transactionRepository.findAll());
            return "transactions";
        }
    }
}