package com.findmytutor.findmytutor_backend.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.findmytutor.findmytutor_backend.model.PasswordResetToken;
import com.findmytutor.findmytutor_backend.model.User;
import com.findmytutor.findmytutor_backend.repository.PasswordResetTokenRepository;
import com.findmytutor.findmytutor_backend.repository.UserRepository;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            BCryptPasswordEncoder passwordEncoder,
            EmailService emailService) {

        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public boolean createResetToken(String email) {

        User user = userRepository.findByEmail(email)
                .orElse(null);

        /*
         * Do not reveal whether an email exists.
         */
        if (user == null) {
            return false;
        }

        /*
         * Remove any previous reset token.
         */
        tokenRepository.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken =
                new PasswordResetToken();

        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(
                LocalDateTime.now().plusMinutes(30)
        );

        tokenRepository.save(resetToken);

        /*
         * Send reset link to the user's email.
         */
        emailService.sendPasswordResetEmail(
                user.getEmail(),
                token
        );

        return true;
    }

    public boolean resetPassword(
            String token,
            String newPassword) {

        PasswordResetToken resetToken =
                tokenRepository.findByToken(token)
                        .orElse(null);

        if (resetToken == null) {
            return false;
        }

        if (resetToken.getExpiryDate()
                .isBefore(LocalDateTime.now())) {

            tokenRepository.delete(resetToken);
            return false;
        }

        User user = resetToken.getUser();

        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        userRepository.save(user);

        /*
         * Token can only be used once.
         */
        tokenRepository.delete(resetToken);

        return true;
    }
}