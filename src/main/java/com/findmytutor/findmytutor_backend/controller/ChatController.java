package com.findmytutor.findmytutor_backend.controller;

import com.findmytutor.findmytutor_backend.dto.ChatMessageResponse;
import com.findmytutor.findmytutor_backend.model.ChatMessage;
import com.findmytutor.findmytutor_backend.model.Session;
import com.findmytutor.findmytutor_backend.model.User;
import com.findmytutor.findmytutor_backend.repository.ChatMessageRepository;
import com.findmytutor.findmytutor_backend.repository.SessionRepository;
import com.findmytutor.findmytutor_backend.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public ChatController(
            ChatMessageRepository chatMessageRepository,
            SessionRepository sessionRepository,
            UserRepository userRepository
    ) {
        this.chatMessageRepository = chatMessageRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<?> getMessages(
            @PathVariable Long sessionId,
            Authentication authentication
    ) {

        try {

            User user = getAuthenticatedUser(authentication);

            Session session = sessionRepository
                    .findById(sessionId)
                    .orElseThrow(() ->
                            new RuntimeException("Session not found"));

            if (!isParticipant(session, user)) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("You are not part of this session.");
            }

            if (!"PAID".equals(session.getStatus())) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("Chat is available after payment.");
            }

            List<ChatMessageResponse> response =
                    chatMessageRepository
                            .findBySessionOrderByCreatedAtAsc(session)
                            .stream()
                            .map(message ->
                                    new ChatMessageResponse(
                                            message.getId(),
                                            message.getSender().getId(),
                                            message.getSender().getName(),
                                            message.getMessage(),
                                            message.getCreatedAt()
                                    )
                            )
                            .toList();

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/{sessionId}")
    public ResponseEntity<?> sendMessage(
            @PathVariable Long sessionId,
            @RequestBody MessageRequest request,
            Authentication authentication
    ) {

        try {

            User user = getAuthenticatedUser(authentication);

            if (request.message() == null ||
                    request.message().trim().isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body("Message cannot be empty.");
            }

            Session session = sessionRepository
                    .findById(sessionId)
                    .orElseThrow(() ->
                            new RuntimeException("Session not found"));

            if (!isParticipant(session, user)) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("You are not part of this session.");
            }

            if (!"PAID".equals(session.getStatus())) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("Chat is available after payment.");
            }

            ChatMessage chatMessage =
                    new ChatMessage();

            chatMessage.setSession(session);
            chatMessage.setSender(user);
            chatMessage.setMessage(
                    request.message().trim()
            );

            ChatMessage saved =
                    chatMessageRepository.save(chatMessage);

            return ResponseEntity.ok(
                    new ChatMessageResponse(
                            saved.getId(),
                            user.getId(),
                            user.getName(),
                            saved.getMessage(),
                            saved.getCreatedAt()
                    )
            );

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    private User getAuthenticatedUser(
            Authentication authentication
    ) {

        String email = authentication.getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Authenticated user not found"
                        ));
    }

    private boolean isParticipant(
            Session session,
            User user
    ) {

        if (session.getParent() != null &&
                session.getParent()
                        .getId()
                        .equals(user.getId())) {

            return true;
        }

        if (session.getTutor() != null &&
                session.getTutor()
                        .getUser()
                        .getId()
                        .equals(user.getId())) {

            return true;
        }

        return false;
    }

    public record MessageRequest(String message) {
    }
}