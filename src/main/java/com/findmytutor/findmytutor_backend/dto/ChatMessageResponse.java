package com.findmytutor.findmytutor_backend.dto;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long id,
        Long senderId,
        String senderName,
        String senderEmail,
        String message,
        LocalDateTime createdAt
) {
}