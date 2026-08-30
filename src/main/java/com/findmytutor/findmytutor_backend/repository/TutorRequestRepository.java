package com.findmytutor.findmytutor_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.findmytutor.findmytutor_backend.model.TutorRequest;

public interface TutorRequestRepository
        extends JpaRepository<TutorRequest, Long> {

    // All requests made by a parent
    List<TutorRequest> findByParentId(Long parentId);

    // All requests received by a tutor
    List<TutorRequest> findByTutorId(Long tutorId);

    // Requests by status
    List<TutorRequest> findByTutorIdAndStatus(
            Long tutorId,
            String status
    );

    List<TutorRequest> findByParentIdAndStatus(
            Long parentId,
            String status
    );
}