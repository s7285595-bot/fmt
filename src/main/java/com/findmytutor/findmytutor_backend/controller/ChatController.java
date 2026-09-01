package com.findmytutor.findmytutor_backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.findmytutor.findmytutor_backend.dto.ChatMessageResponse;
import com.findmytutor.findmytutor_backend.model.ChatMessage;
import com.findmytutor.findmytutor_backend.model.Session;
import com.findmytutor.findmytutor_backend.model.User;
import com.findmytutor.findmytutor_backend.repository.ChatMessageRepository;
import com.findmytutor.findmytutor_backend.repository.SessionRepository;
import com.findmytutor.findmytutor_backend.repository.UserRepository;

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

    // =========================================================
    // GET CHAT MESSAGES
    // =========================================================

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

            // Only parent or tutor belonging to this session
            // can access the chat.
            if (!isParticipant(session, user)) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("You are not part of this session.");
            }

            // Chat allowed for confirmed demo or paid session.
            if (!isChatAllowed(session)) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(
                                "Chat is available after the session is confirmed."
                        );
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
                                            message.getSender().getEmail(),
                                            message.getMessage(),
                                            message.getCreatedAt()
                                    )
                            )
                            .toList();

            // Mark messages from the other participant as read
            markMessagesAsRead(session, user);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // =========================================================
    // SEND CHAT MESSAGE
    // =========================================================

    @PostMapping("/{sessionId}")
    public ResponseEntity<?> sendMessage(
            @PathVariable Long sessionId,
            @RequestBody MessageRequest request,
            Authentication authentication
    ) {

        try {

            User user = getAuthenticatedUser(authentication);

            // Validate message
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

            // Only parent or tutor belonging to this session
            // can send messages.
            if (!isParticipant(session, user)) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("You are not part of this session.");
            }

            // Chat allowed for confirmed demo or paid session.
            if (!isChatAllowed(session)) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(
                                "Chat is available after the session is confirmed."
                        );
            }

            // Create message
            ChatMessage chatMessage = new ChatMessage();

            chatMessage.setSession(session);
            chatMessage.setSender(user);
            chatMessage.setMessage(
                    request.message().trim()
            );

            // New message starts as unread
            chatMessage.setRead(false);

            ChatMessage saved =
                    chatMessageRepository.save(chatMessage);

            return ResponseEntity.ok(
                    new ChatMessageResponse(
                            saved.getId(),
                            user.getId(),
                            user.getName(),
                            user.getEmail(),
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

    // =========================================================
    // GET TOTAL UNREAD MESSAGE COUNT
    // =========================================================

    @GetMapping("/unread/count")
    public ResponseEntity<?> getUnreadCount(
            Authentication authentication
    ) {

        try {

            User user = getAuthenticatedUser(authentication);

            long unreadCount = 0;

            // =================================================
            // PARENT
            // =================================================

            if ("PARENT".equalsIgnoreCase(user.getRole())) {

                List<Session> sessions =
                        sessionRepository.findByParentId(user.getId());

                for (Session session : sessions) {

                    unreadCount +=
                            chatMessageRepository
                                    .countBySessionAndSenderIdNotAndReadFalse(
                                            session,
                                            user.getId()
                                    );
                }
            }

            // =================================================
            // TUTOR
            // =================================================

            else if ("TUTOR".equalsIgnoreCase(user.getRole())) {

                List<Session> sessions =
                        sessionRepository.findByTutorId(user.getId());

                for (Session session : sessions) {

                    unreadCount +=
                            chatMessageRepository
                                    .countBySessionAndSenderIdNotAndReadFalse(
                                            session,
                                            user.getId()
                                    );
                }
            }

            return ResponseEntity.ok(
                    new UnreadCountResponse(unreadCount)
            );

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // =========================================================
    // MARK SESSION MESSAGES AS READ
    // =========================================================

    @PostMapping("/{sessionId}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long sessionId,
            Authentication authentication
    ) {

        try {

            User user = getAuthenticatedUser(authentication);

            Session session = sessionRepository
                    .findById(sessionId)
                    .orElseThrow(() ->
                            new RuntimeException("Session not found"));

            // Only parent or tutor belonging to this session
            // can mark messages as read.
            if (!isParticipant(session, user)) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("You are not part of this session.");
            }

            markMessagesAsRead(session, user);

            return ResponseEntity.ok(
                    new MessageResponse("Messages marked as read.")
            );

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // =========================================================
    // MARK OTHER PARTICIPANT'S MESSAGES AS READ
    // =========================================================

    private void markMessagesAsRead(
            Session session,
            User currentUser
    ) {

        List<ChatMessage> unreadMessages =
                chatMessageRepository
                        .findBySessionAndSenderIdNotAndReadFalse(
                                session,
                                currentUser.getId()
                        );

        if (unreadMessages.isEmpty()) {
            return;
        }

        for (ChatMessage message : unreadMessages) {
            message.setRead(true);
        }

        chatMessageRepository.saveAll(unreadMessages);
    }

    // =========================================================
    // CHAT ACCESS RULE
    // =========================================================

    private boolean isChatAllowed(Session session) {

        String status = session.getStatus();

        // Demo accepted/confirmed
        if ("DEMO_CONFIRMED".equals(status)) {
            return true;
        }

        // Paid session
        if ("PAID".equals(status)) {
            return true;
        }

        // Active session
        if ("ACTIVE".equals(status)) {
            return true;
        }

        return false;
    }

    // =========================================================
    // GET AUTHENTICATED USER
    // =========================================================

    private User getAuthenticatedUser(
            Authentication authentication
    ) {

        if (authentication == null) {
            throw new RuntimeException(
                    "User is not authenticated"
            );
        }

        String email = authentication.getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Authenticated user not found"
                        ));
    }

    // =========================================================
    // CHECK SESSION PARTICIPANT
    // =========================================================

    private boolean isParticipant(
            Session session,
            User user
    ) {

        // Parent
        if (session.getParent() != null &&
                session.getParent()
                        .getId()
                        .equals(user.getId())) {

            return true;
        }

        // Tutor
        if (session.getTutor() != null &&
                session.getTutor().getUser() != null &&
                session.getTutor()
                        .getUser()
                        .getId()
                        .equals(user.getId())) {

            return true;
        }

        return false;
    }

    // =========================================================
    // RESPONSE RECORDS
    // =========================================================

    public record MessageRequest(String message) {
    }

    public record UnreadCountResponse(long count) {
    }

    public record MessageResponse(String message) {
    }
}