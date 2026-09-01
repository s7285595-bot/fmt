package com.findmytutor.findmytutor_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(
            String email,
            String token) {

        String resetLink =
                frontendUrl + "/reset-password?token=" + token;

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject(
                "FindMyTutor - Reset your password"
        );

        message.setText(
                "Hello,\n\n" +
                "We received a request to reset your " +
                "FindMyTutor password.\n\n" +
                "Click the link below to create a new password:\n\n" +
                resetLink +
                "\n\n" +
                "This link will expire in 30 minutes.\n\n" +
                "If you did not request a password reset, " +
                "you can safely ignore this email.\n\n" +
                "Regards,\n" +
                "FindMyTutor Team"
        );

        mailSender.send(message);
    }
}