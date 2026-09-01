package com.findmytutor.findmytutor_backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Session being paid for
    @OneToOne
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private Session session;

    // Parent who made the payment
    @ManyToOne
    @JoinColumn(name = "parent_id", nullable = false)
    private User parent;

    // Tutor receiving the payment
    @ManyToOne
    @JoinColumn(name = "tutor_id", nullable = false)
    private Tutor tutor;

    // Total amount paid by parent
    @Column(nullable = false)
    private Double amount;

    // Platform commission
    @Column(nullable = false)
    private Double platformFee;

    // Amount that goes to tutor
    @Column(nullable = false)
    private Double tutorAmount;

    // PENDING / PAID / FAILED / REFUNDED
    @Column(nullable = false)
    private String status = "PENDING";

    // UPI / CARD / etc.
    @Column(nullable = false)
    private String paymentMethod;

    // Payment gateway transaction ID
    private String transactionId;

    // When payment record was created
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // When payment was successfully completed
    private LocalDateTime paidAt;

    public Payment() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public User getParent() {
        return parent;
    }

    public void setParent(User parent) {
        this.parent = parent;
    }

    public Tutor getTutor() {
        return tutor;
    }

    public void setTutor(Tutor tutor) {
        this.tutor = tutor;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getPlatformFee() {
        return platformFee;
    }

    public void setPlatformFee(Double platformFee) {
        this.platformFee = platformFee;
    }

    public Double getTutorAmount() {
        return tutorAmount;
    }

    public void setTutorAmount(Double tutorAmount) {
        this.tutorAmount = tutorAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}