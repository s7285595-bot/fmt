
package com.findmytutor.findmytutor_backend.controller;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.findmytutor.findmytutor_backend.model.Payment;
import com.findmytutor.findmytutor_backend.model.Session;
import com.findmytutor.findmytutor_backend.model.User;
import com.findmytutor.findmytutor_backend.repository.PaymentRepository;
import com.findmytutor.findmytutor_backend.repository.SessionRepository;
import com.findmytutor.findmytutor_backend.repository.UserRepository;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    // Platform commission
    // Example: 10%
    private static final double PLATFORM_FEE_PERCENT = 10.0;

    public PaymentController(
            PaymentRepository paymentRepository,
            SessionRepository sessionRepository,
            UserRepository userRepository) {

        this.paymentRepository = paymentRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    // ==========================================
    // PARENT - PAY FOR SESSION
    // ==========================================

    @PostMapping("/session/{sessionId}")
    public ResponseEntity<?> payForSession(
            @PathVariable Long sessionId,
            Authentication authentication) {

        String email = authentication.getName();

        User parent = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // Only parents can make payments
        if (!"PARENT".equals(parent.getRole())) {
            return ResponseEntity.status(403)
                    .body("Only parents can make payments");
        }

        // Find session
        Session session = sessionRepository
                .findById(sessionId)
                .orElse(null);

        if (session == null) {
            return ResponseEntity.notFound().build();
        }

        // Make sure this session belongs to this parent
        if (!session.getParent().getId().equals(parent.getId())) {
            return ResponseEntity.status(403)
                    .body("You cannot pay for this session");
        }

        // Session must be confirmed
        if (!"CONFIRMED".equals(session.getStatus())) {
            return ResponseEntity.badRequest()
                    .body("This session is not available for payment");
        }

        // Check if payment already exists
        if (paymentRepository.findBySessionId(sessionId).isPresent()) {
            return ResponseEntity.badRequest()
                    .body("Payment already exists for this session");
        }

        // ==========================================
        // CALCULATE PAYMENT
        // ==========================================

        Double amount = session.getTotalAmount();

        Double platformFee =
                amount * PLATFORM_FEE_PERCENT / 100;

        Double tutorAmount =
                amount - platformFee;

        // ==========================================
        // CREATE PAYMENT
        // ==========================================

        Payment payment = new Payment();

        payment.setSession(session);
        payment.setParent(parent);
        payment.setTutor(session.getTutor());

        payment.setAmount(amount);
        payment.setPlatformFee(platformFee);
        payment.setTutorAmount(tutorAmount);

        // Demo payment is immediately successful
        payment.setStatus("PAID");
        payment.setPaidAt(LocalDateTime.now());

        Payment savedPayment =
                paymentRepository.save(payment);

        // ==========================================
        // UPDATE SESSION
        // ==========================================

        session.setStatus("PAID");

        sessionRepository.save(session);

        return ResponseEntity.ok(savedPayment);
    }

    // ==========================================
    // PARENT - GET PAYMENT
    // ==========================================

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<?> getPayment(
            @PathVariable Long sessionId,
            Authentication authentication) {

        String email = authentication.getName();

        User parent = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (!"PARENT".equals(parent.getRole())) {
            return ResponseEntity.status(403)
                    .body("Only parents can view payments");
        }

        Payment payment = paymentRepository
                .findBySessionId(sessionId)
                .orElse(null);

        if (payment == null) {
            return ResponseEntity.notFound().build();
        }

        // Make sure payment belongs to this parent
        if (!payment.getParent().getId().equals(parent.getId())) {
            return ResponseEntity.status(403)
                    .body("You cannot view this payment");
        }

        return ResponseEntity.ok(payment);
    }
}
