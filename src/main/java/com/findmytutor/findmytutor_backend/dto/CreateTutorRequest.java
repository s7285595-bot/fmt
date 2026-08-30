package com.findmytutor.findmytutor_backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class CreateTutorRequest {

    private Long tutorId;
    private String subject;
    private LocalDate requestedDate;
    private LocalTime requestedTime;
    private Integer hours;
    private String message;

    public CreateTutorRequest() {
    }

    public Long getTutorId() {
        return tutorId;
    }

    public void setTutorId(Long tutorId) {
        this.tutorId = tutorId;
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
}