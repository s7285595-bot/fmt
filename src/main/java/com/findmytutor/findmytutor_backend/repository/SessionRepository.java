package com.findmytutor.findmytutor_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.findmytutor.findmytutor_backend.model.Session;

public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByParentId(Long parentId);

    List<Session> findByTutorId(Long tutorId);
}