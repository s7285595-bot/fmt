package com.findmytutor.findmytutor_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.findmytutor.findmytutor_backend.model.ChatMessage;
import com.findmytutor.findmytutor_backend.model.Session;

public interface ChatMessageRepository
        extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySessionOrderByCreatedAtAsc(
            Session session
    );

    long countBySessionAndSenderIdNotAndReadFalse(
            Session session,
            Long senderId
    );

    List<ChatMessage> findBySessionAndSenderIdNotAndReadFalse(
            Session session,
            Long senderId
    );
}