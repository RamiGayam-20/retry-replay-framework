package com.example.retry.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private JavaMailSender mailSender;

    public void sendNotification(String to, String subject, String message) {
        logger.debug("Attempting to send email to {}, subject: {}", to, subject);
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setSubject(subject);
        mail.setText(message);
        try {
            mailSender.send(mail);
            logger.info("Notification sent to {}", to);
        } catch (Exception e) {
            logger.error("Failed to send notification to {}. Error: {}", to, e.getMessage(), e);
            throw e;
        }
    }
}