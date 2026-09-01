package com.findmytutor.findmytutor_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.findmytutor.findmytutor_backend.model.Session;
import com.findmytutor.findmytutor_backend.model.Tutor;
import com.findmytutor.findmytutor_backend.model.TutorRequest;
import com.findmytutor.findmytutor_backend.model.User;
import com.findmytutor.findmytutor_backend.repository.SessionRepository;
import com.findmytutor.findmytutor_backend.repository.TutorRepository;
import com.findmytutor.findmytutor_backend.repository.TutorRequestRepository;
import com.findmytutor.findmytutor_backend.repository.UserRepository;

@RestController
@RequestMapping("/api/requests")
public class TutorRequestController {

    private final TutorRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final TutorRepository tutorRepository;
    private final SessionRepository sessionRepository;

    public TutorRequestController(
            TutorRequestRepository requestRepository,
            UserRepository userRepository,
            TutorRepository tutorRepository,
            SessionRepository sessionRepository) {

        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.tutorRepository = tutorRepository;
        this.sessionRepository = sessionRepository;
    }

    // ==========================================
    // CREATE REQUEST - PARENT
    // ==========================================

    @PostMapping("/tutor/{tutorId}")
    public ResponseEntity<?> createRequest(
            @PathVariable Long tutorId,
            @RequestBody TutorRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        User parent = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (!"PARENT".equals(parent.getRole())) {
            return ResponseEntity.status(403)
                    .body("Only parents can send tutor requests");
        }

        Tutor tutor = tutorRepository.findById(tutorId)
                .orElse(null);

        if (tutor == null) {
            return ResponseEntity.notFound().build();
        }

        if (tutor.getUser() == null ||
                !"APPROVED".equals(tutor.getUser().getStatus())) {

            return ResponseEntity.badRequest()
                    .body("Tutor is not approved");
        }

        request.setParent(parent);
        request.setTutor(tutor);

        if (request.getType() == null ||
                request.getType().isBlank()) {

            request.setType("DEMO");
        }

        request.setStatus("PENDING");

        TutorRequest savedRequest =
                requestRepository.save(request);

        return ResponseEntity.ok(savedRequest);
    }

    // ==========================================
    // PARENT - MY REQUESTS
    // ==========================================

    @GetMapping("/parent")
    public ResponseEntity<?> getParentRequests(
            Authentication authentication) {

        String email = authentication.getName();

        User parent = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (!"PARENT".equals(parent.getRole())) {
            return ResponseEntity.status(403)
                    .body("Only parents can view parent requests");
        }

        List<TutorRequest> requests =
                requestRepository.findByParentId(parent.getId());

        return ResponseEntity.ok(requests);
    }

    // ==========================================
    // TUTOR - RECEIVED REQUESTS
    // ==========================================

    @GetMapping("/tutor")
    public ResponseEntity<?> getTutorRequests(
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (!"TUTOR".equals(user.getRole())) {
            return ResponseEntity.status(403)
                    .body("Only tutors can view tutor requests");
        }

        Tutor tutor = tutorRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Tutor profile not found"));

        List<TutorRequest> requests =
                requestRepository.findByTutorId(tutor.getId());

        return ResponseEntity.ok(requests);
    }

    // ==========================================
    // TUTOR - ACCEPT REQUEST
    // ==========================================

    @PostMapping("/{requestId}/accept")
    public ResponseEntity<?> acceptRequest(
            @PathVariable Long requestId,
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (!"TUTOR".equals(user.getRole())) {
            return ResponseEntity.status(403)
                    .body("Only tutors can accept requests");
        }

        Tutor tutor = tutorRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Tutor profile not found"));

        TutorRequest request = requestRepository
                .findById(requestId)
                .orElse(null);

        if (request == null) {
            return ResponseEntity.notFound().build();
        }

        // Make sure request belongs to this tutor
        if (!request.getTutor().getId().equals(tutor.getId())) {
            return ResponseEntity.status(403)
                    .body("You cannot modify this request");
        }

        // Must be pending
        if (!"PENDING".equals(request.getStatus())) {
            return ResponseEntity.badRequest()
                    .body("This request is no longer pending");
        }

        // ==========================================
        // ACCEPT REQUEST
        // ==========================================

        request.setStatus("ACCEPTED");

        requestRepository.save(request);

        // ==========================================
        // CREATE SESSION
        // ==========================================

        Session session = new Session();

        session.setParent(request.getParent());
        session.setTutor(request.getTutor());
        session.setRequest(request);

        session.setSubject(request.getSubject());
        session.setSessionDate(request.getRequestedDate());
        session.setSessionTime(request.getRequestedTime());
        session.setHours(request.getHours());

        Double hourlyFee = tutor.getHourlyFee();

        session.setHourlyFee(hourlyFee);

        Double totalAmount =
                hourlyFee * request.getHours();

        session.setTotalAmount(totalAmount);

        // ==========================================
        // DEMO SESSION
        // ==========================================

        if ("DEMO".equalsIgnoreCase(request.getType())) {

            session.setType("DEMO");
            session.setStatus("DEMO_CONFIRMED");

        } else {

            // ==========================================
            // REGULAR SESSION
            // ==========================================

            session.setType("REGULAR");
            session.setStatus("CONFIRMED");
        }

        Session savedSession =
                sessionRepository.save(session);

        return ResponseEntity.ok(savedSession);
    }

    // ==========================================
    // TUTOR - REJECT REQUEST
    // ==========================================

    @PostMapping("/{requestId}/reject")
    public ResponseEntity<?> rejectRequest(
            @PathVariable Long requestId,
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (!"TUTOR".equals(user.getRole())) {
            return ResponseEntity.status(403)
                    .body("Only tutors can reject requests");
        }

        Tutor tutor = tutorRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Tutor profile not found"));

        TutorRequest request = requestRepository
                .findById(requestId)
                .orElse(null);

        if (request == null) {
            return ResponseEntity.notFound().build();
        }

        if (!request.getTutor().getId().equals(tutor.getId())) {
            return ResponseEntity.status(403)
                    .body("You cannot modify this request");
        }

        if (!"PENDING".equals(request.getStatus())) {
            return ResponseEntity.badRequest()
                    .body("This request is no longer pending");
        }

        request.setStatus("REJECTED");

        requestRepository.save(request);

        return ResponseEntity.ok(
                "Tutor request rejected successfully"
        );
    }

    // ==========================================
    // PARENT - CONTINUE WITH TUTOR
    //
    // IMPORTANT:
    // We receive SESSION ID here.
    //
    // Demo:
    // DEMO_CONFIRMED
    //
    // Parent clicks:
    // Continue with Tutor
    //
    // We create a NEW regular request.
    // Tutor must accept it.
    // ==========================================

    @PostMapping("/session/{sessionId}/continue")
    public ResponseEntity<?> continueWithTutor(
            @PathVariable Long sessionId,
            @RequestBody TutorRequest requestData,
            Authentication authentication) {

        String email = authentication.getName();

        User parent = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (!"PARENT".equals(parent.getRole())) {
            return ResponseEntity.status(403)
                    .body("Only parents can continue with a tutor");
        }

        // Find demo session
        Session demoSession = sessionRepository
                .findById(sessionId)
                .orElse(null);

        if (demoSession == null) {
            return ResponseEntity.notFound().build();
        }

        // Make sure this session belongs to parent
        if (!demoSession.getParent().getId().equals(parent.getId())) {
            return ResponseEntity.status(403)
                    .body("You cannot continue this session");
        }

        // Must be a demo
        if (!"DEMO".equalsIgnoreCase(demoSession.getType())) {
            return ResponseEntity.badRequest()
                    .body("This is not a demo session");
        }

        // Demo must be confirmed
        if (!"DEMO_CONFIRMED".equals(demoSession.getStatus())) {
            return ResponseEntity.badRequest()
                    .body("Demo session is not confirmed");
        }

        Tutor tutor = demoSession.getTutor();

        // ==========================================
        // CREATE REGULAR SESSION REQUEST
        // ==========================================

        TutorRequest sessionRequest =
                new TutorRequest();

        sessionRequest.setParent(parent);
        sessionRequest.setTutor(tutor);

        sessionRequest.setSubject(
                requestData.getSubject() != null
                        ? requestData.getSubject()
                        : demoSession.getSubject()
        );

        sessionRequest.setRequestedDate(
                requestData.getRequestedDate() != null
                        ? requestData.getRequestedDate()
                        : demoSession.getSessionDate()
        );

        sessionRequest.setRequestedTime(
                requestData.getRequestedTime() != null
                        ? requestData.getRequestedTime()
                        : demoSession.getSessionTime()
        );

        sessionRequest.setHours(
                requestData.getHours() != null
                        ? requestData.getHours()
                        : demoSession.getHours()
        );

        sessionRequest.setMessage(
                requestData.getMessage()
        );

        // This is a regular paid session
        sessionRequest.setType("SESSION");

        // Tutor must approve
        sessionRequest.setStatus("PENDING");

        TutorRequest savedRequest =
                requestRepository.save(sessionRequest);

        return ResponseEntity.ok(savedRequest);
    }
}