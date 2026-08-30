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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(
            UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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
public ResponseEntity<?> login(@RequestBody LoginRequest request) {

    User user = userRepository.findByEmail(request.getEmail())
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
}