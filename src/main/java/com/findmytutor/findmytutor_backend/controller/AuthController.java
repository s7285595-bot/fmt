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

        user.setRole(user.getRole().toUpperCase());

        if (!user.getRole().equals("PARENT")
                && !user.getRole().equals("TUTOR")) {

            return ResponseEntity.badRequest()
                    .body("Role must be PARENT or TUTOR");
        }

        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
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

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole());

        return ResponseEntity.ok(token);
    }
}