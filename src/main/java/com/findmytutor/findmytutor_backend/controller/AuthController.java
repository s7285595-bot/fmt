package com.findmytutor.findmytutor_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.findmytutor.findmytutor_backend.dto.LoginRequest;
import com.findmytutor.findmytutor_backend.model.User;
import com.findmytutor.findmytutor_backend.repository.UserRepository;
import com.findmytutor.findmytutor_backend.security.JwtService;
import com.findmytutor.findmytutor_backend.service.PasswordResetService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PasswordResetService passwordResetService;

    public AuthController(
            UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder,
            JwtService jwtService,
            PasswordResetService passwordResetService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {

        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest()
                    .body("Email already registered");
        }

        if (user.getRole() == null) {
            return ResponseEntity.badRequest()
                    .body("Role is required");
        }

        user.setRole(user.getRole().toUpperCase());

        // Public registration only allows PARENT or TUTOR
        if (!user.getRole().equals("PARENT")
                && !user.getRole().equals("TUTOR")) {

            return ResponseEntity.badRequest()
                    .body("Role must be PARENT or TUTOR");
        }

        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        // Parent can use the app immediately
        if (user.getRole().equals("PARENT")) {
            user.setStatus("APPROVED");
        }

        // Tutor must wait for admin approval
        if (user.getRole().equals("TUTOR")) {
            user.setStatus("PENDING");
        }

        userRepository.save(user);

        if (user.getRole().equals("TUTOR")) {
            return ResponseEntity.ok(
                    "Tutor registration submitted. Await admin approval."
            );
        }

        return ResponseEntity.ok(
                "Parent registered successfully."
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(401)
                    .body("Invalid email or password");
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            return ResponseEntity.status(401)
                    .body("Invalid email or password");
        }

        /*
         * =========================
         * TUTOR ACCOUNT STATUS
         * =========================
         */

        if ("TUTOR".equals(user.getRole())) {

            if ("PENDING".equals(user.getStatus())) {
                return ResponseEntity.status(403)
                        .body(
                                "Your tutor application is under review. " +
                                "Please wait for admin approval."
                        );
            }

            if ("REJECTED".equals(user.getStatus())) {
                return ResponseEntity.status(403)
                        .body(
                                "Your tutor application has been rejected."
                        );
            }

            if ("SUSPENDED".equals(user.getStatus())) {
                return ResponseEntity.status(403)
                        .body(
                                "Your tutor account has been suspended. " +
                                "Please contact support."
                        );
            }

            if ("BLOCKED".equals(user.getStatus())) {
                return ResponseEntity.status(403)
                        .body(
                                "Your tutor account has been blocked."
                        );
            }

            if (!"APPROVED".equals(user.getStatus())) {
                return ResponseEntity.status(403)
                        .body(
                                "Your tutor account is not approved yet."
                        );
            }
        }

        /*
         * =========================
         * PARENT ACCOUNT STATUS
         * =========================
         */

        if ("SUSPENDED".equals(user.getStatus())) {
            return ResponseEntity.status(403)
                    .body(
                            "Your account has been suspended. " +
                            "Please contact support."
                    );
        }

        if ("BLOCKED".equals(user.getStatus())) {
            return ResponseEntity.status(403)
                    .body(
                            "Your account has been blocked."
                    );
        }

        /*
         * =========================
         * GENERATE JWT
         * =========================
         */

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole()
        );

        return ResponseEntity.ok(token);
    }

    /*
     * =========================
     * FORGOT PASSWORD
     * =========================
     */

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @RequestBody ForgotPasswordRequest request) {

        String token = passwordResetService
                .createResetToken(request.getEmail());

        if (token == null) {
            return ResponseEntity.ok(
                    "If an account exists with this email, " +
                    "a password reset link has been created."
            );
        }

        /*
         * Temporary development response.
         *
         * We will replace this with an email
         * after SMTP/email configuration is added.
         */
        return ResponseEntity.ok(
                "Password reset token created: " + token
        );
    }

    /*
     * =========================
     * RESET PASSWORD
     * =========================
     */

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestBody ResetPasswordRequest request) {

        if (request.getToken() == null
                || request.getToken().isBlank()) {

            return ResponseEntity.badRequest()
                    .body("Reset token is required");
        }

        if (request.getNewPassword() == null
                || request.getNewPassword().length() < 6) {

            return ResponseEntity.badRequest()
                    .body(
                            "Password must be at least 6 characters"
                    );
        }

        boolean success = passwordResetService.resetPassword(
                request.getToken(),
                request.getNewPassword()
        );

        if (!success) {
            return ResponseEntity.badRequest()
                    .body(
                            "Invalid or expired password reset token"
                    );
        }

        return ResponseEntity.ok(
                "Password reset successfully"
        );
    }

    /*
     * =========================
     * REQUEST CLASSES
     * =========================
     */

    public static class ForgotPasswordRequest {

        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public static class ResetPasswordRequest {

        private String token;
        private String newPassword;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}