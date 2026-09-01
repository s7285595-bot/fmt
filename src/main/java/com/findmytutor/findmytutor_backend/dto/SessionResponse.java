package com.findmytutor.findmytutor_backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.findmytutor.findmytutor_backend.model.Session;

public class SessionResponse {

    private Long id;

    private Long parentId;
    private String parentName;

    private Long tutorId;
    private String tutorName;

    private Long requestId;

    private String subject;
    private LocalDate sessionDate;
    private LocalTime sessionTime;
    private Integer hours;

    private Double hourlyFee;
    private Double totalAmount;

    private String type;
    private String status;

    public SessionResponse(Session session) {

        this.id = session.getId();

        this.parentId = session.getParent().getId();
        this.parentName = session.getParent().getName();

        this.tutorId = session.getTutor().getId();
        this.tutorName = session.getTutor().getUser().getName();

        this.requestId = session.getRequest().getId();

        this.subject = session.getSubject();
        this.sessionDate = session.getSessionDate();
        this.sessionTime = session.getSessionTime();
        this.hours = session.getHours();

        this.hourlyFee = session.getHourlyFee();
        this.totalAmount = session.getTotalAmount();

        this.type = session.getType();
        this.status = session.getStatus();
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

    public Long getRequestId() {
        return requestId;
    }

    public String getSubject() {
        return subject;
    }

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public LocalTime getSessionTime() {
        return sessionTime;
    }

    public Integer getHours() {
        return hours;
    }

    public Double getHourlyFee() {
        return hourlyFee;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }
}