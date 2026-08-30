
package com.findmytutor.findmytutor_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.findmytutor.findmytutor_backend.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findBySessionId(Long sessionId);

}

