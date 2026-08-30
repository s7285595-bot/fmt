package com.findmytutor.findmytutor_backend.model;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tutor_requests")
public class TutorRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Parent who is requesting the tutor
    @ManyToOne
    @JoinColumn(name = "parent_id", nullable = false)
    private User parent;

    // Tutor being requested
    @ManyToOne
    @JoinColumn(name = "tutor_id", nullable = false)
    private Tutor tutor;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private LocalDate requestedDate;

    @Column(nullable = false)
    private LocalTime requestedTime;

    @Column(nullable = false)
    private Integer hours;

    @Column(length = 1000)
    private String message;

    @Column(nullable = false)
private String type = "DEMO";
    @Column(nullable = false)
    private String status = "PENDING";

    public TutorRequest() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public LocalDate getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(LocalDate requestedDate) {
        this.requestedDate = requestedDate;
    }

    public LocalTime getRequestedTime() {
        return requestedTime;
    }

    public void setRequestedTime(LocalTime requestedTime) {
        this.requestedTime = requestedTime;
    }

    public Integer getHours() {
        return hours;
    }

    public void setHours(Integer hours) {
        this.hours = hours;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public String getType() {
    return type;
}

public void setType(String type) {
    this.type = type;
}

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}