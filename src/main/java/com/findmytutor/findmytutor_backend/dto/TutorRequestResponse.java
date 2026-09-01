package com.findmytutor.findmytutor_backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.findmytutor.findmytutor_backend.model.TutorRequest;

public class TutorRequestResponse {

    private Long id;

    private Long parentId;
    private String parentName;

    private Long tutorId;
    private String tutorName;

    private String subject;
    private LocalDate requestedDate;
    private LocalTime requestedTime;
    private Integer hours;
    private String message;

    private String type;
    private String status;

    public TutorRequestResponse(TutorRequest request) {

        this.id = request.getId();

        this.parentId = request.getParent().getId();
        this.parentName = request.getParent().getName();

        this.tutorId = request.getTutor().getId();
        this.tutorName = request.getTutor().getUser().getName();

        this.subject = request.getSubject();
        this.requestedDate = request.getRequestedDate();
        this.requestedTime = request.getRequestedTime();
        this.hours = request.getHours();
        this.message = request.getMessage();

        this.type = request.getType();
        this.status = request.getStatus();
    }

    public Long getId() {
        return id;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getParentName() {
        return parentName;
    }

    public Long getTutorId() {
        return tutorId;
    }

    public String getTutorName() {
        return tutorName;
    }

    public String getSubject() {
        return subject;
    }

    public LocalDate getRequestedDate() {
        return requestedDate;
    }

    public LocalTime getRequestedTime() {
        return requestedTime;
    }

    public Integer getHours() {
        return hours;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }
}