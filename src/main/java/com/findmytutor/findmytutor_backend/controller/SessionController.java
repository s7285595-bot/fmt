package com.findmytutor.findmytutor_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.findmytutor.findmytutor_backend.model.Session;
import com.findmytutor.findmytutor_backend.model.Tutor;
import com.findmytutor.findmytutor_backend.model.User;
import com.findmytutor.findmytutor_backend.repository.SessionRepository;
import com.findmytutor.findmytutor_backend.repository.TutorRepository;
import com.findmytutor.findmytutor_backend.repository.UserRepository;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final TutorRepository tutorRepository;

    public SessionController(
            SessionRepository sessionRepository,
            UserRepository userRepository,
            TutorRepository tutorRepository) {

        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.tutorRepository = tutorRepository;
    }

    // ==========================================
    // PARENT - MY SESSIONS
    // ==========================================

    @GetMapping("/parent")
    public ResponseEntity<?> getParentSessions(
            Authentication authentication) {

        String email = authentication.getName();

        User parent = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (!"PARENT".equals(parent.getRole())) {
            return ResponseEntity.status(403)
                    .body("Only parents can view sessions");
        }

        List<Session> sessions =
                sessionRepository.findByParentId(parent.getId());

        return ResponseEntity.ok(sessions);
    }

    // ==========================================
    // TUTOR - MY SESSIONS
    // ==========================================

    @GetMapping("/tutor")
    public ResponseEntity<?> getTutorSessions(
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (!"TUTOR".equals(user.getRole())) {
            return ResponseEntity.status(403)
                    .body("Only tutors can view sessions");
        }

        Tutor tutor = tutorRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Tutor profile not found"));

        List<Session> sessions =
                sessionRepository.findByTutorId(tutor.getId());

        return ResponseEntity.ok(sessions);
    }
}