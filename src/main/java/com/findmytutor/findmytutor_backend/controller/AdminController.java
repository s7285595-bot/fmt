package com.findmytutor.findmytutor_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.findmytutor.findmytutor_backend.model.User;
import com.findmytutor.findmytutor_backend.repository.UserRepository;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // GET ALL PENDING TUTORS
    @GetMapping("/tutors/pending")
    public ResponseEntity<?> getPendingTutors() {

        List<User> tutors = userRepository.findAll()
                .stream()
                .filter(user ->
                        "TUTOR".equals(user.getRole()) &&
                        "PENDING".equals(user.getStatus())
                )
                .toList();

        return ResponseEntity.ok(tutors);
    }

    // APPROVE TUTOR
    @PostMapping("/tutors/{userId}/approve")
    public ResponseEntity<?> approveTutor(
            @PathVariable Long userId) {

        User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        if (!"TUTOR".equals(user.getRole())) {
            return ResponseEntity.badRequest()
                    .body("User is not a tutor");
        }

        user.setStatus("APPROVED");

        userRepository.save(user);

        return ResponseEntity.ok(
                "Tutor approved successfully"
        );
    }

    // REJECT TUTOR
    @PostMapping("/tutors/{userId}/reject")
    public ResponseEntity<?> rejectTutor(
            @PathVariable Long userId) {

        User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        if (!"TUTOR".equals(user.getRole())) {
            return ResponseEntity.badRequest()
                    .body("User is not a tutor");
        }

        user.setStatus("REJECTED");

        userRepository.save(user);

        return ResponseEntity.ok(
                "Tutor rejected successfully"
        );
    }

    // SUSPEND USER
    @PostMapping("/users/{userId}/suspend")
    public ResponseEntity<?> suspendUser(
            @PathVariable Long userId) {

        User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        user.setStatus("SUSPENDED");

        userRepository.save(user);

        return ResponseEntity.ok(
                "User suspended successfully"
        );
    }

    // BLOCK USER
    @PostMapping("/users/{userId}/block")
    public ResponseEntity<?> blockUser(
            @PathVariable Long userId) {

        User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        user.setStatus("BLOCKED");

        userRepository.save(user);

        return ResponseEntity.ok(
                "User blocked successfully"
        );
    }

    // GET ALL USERS
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {

        return ResponseEntity.ok(
                userRepository.findAll()
        );
    }
    
    
}
