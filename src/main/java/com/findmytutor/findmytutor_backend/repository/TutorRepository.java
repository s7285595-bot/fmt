package com.findmytutor.findmytutor_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.findmytutor.findmytutor_backend.model.Tutor;

public interface TutorRepository extends JpaRepository<Tutor, Long> {

    Optional<Tutor> findByUserId(Long userId);

    List<Tutor> findBySubjectsContainingIgnoreCase(String subject);
        List<Tutor> findByCityIgnoreCase(String city);


}