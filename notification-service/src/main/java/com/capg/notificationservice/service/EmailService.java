package com.capg.notificationservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void send(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to={} subject={}", to, subject);
        } catch (MailAuthenticationException e) {
            log.error("Email authentication failed - check mail credentials. to={} subject={} error={}", to, subject, e.getMessage());
            // Do not rethrow: retrying will not fix invalid credentials, avoids infinite requeue loop
        } catch (Exception e) {
            log.error("Failed to send email to={} subject={} error={}", to, subject, e.getMessage());
            throw e;
        }
    }
}
