package com.findmytutor.findmytutor_backend.dto;

import com.findmytutor.findmytutor_backend.model.Tutor;

public class TutorResponse {

    private Long id;
    private String name;
    private String email;
    private String qualification;
    private Integer experience;
    private String subjects;
    private Double hourlyFee;
    private String city;
    private String teachingMode;
    private String bio;
    private Double rating;
    private Double distanceKm;

    public TutorResponse(Tutor tutor) {

        this.id = tutor.getId();

        this.name = tutor.getUser().getName();
        this.email = tutor.getUser().getEmail();

        this.qualification = tutor.getQualification();
        this.experience = tutor.getExperience();
        this.subjects = tutor.getSubjects();
        this.hourlyFee = tutor.getHourlyFee();
        this.city = tutor.getCity();
        this.teachingMode = tutor.getTeachingMode();
        this.bio = tutor.getBio();
        this.rating = tutor.getRating();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getQualification() {
        return qualification;
    }

    public Integer getExperience() {
        return experience;
    }

    public String getSubjects() {
        return subjects;
    }

    public Double getHourlyFee() {
        return hourlyFee;
    }

    public String getCity() {
        return city;
    }

    public String getTeachingMode() {
        return teachingMode;
    }

    public String getBio() {
        return bio;
    }

    public Double getRating() {
        return rating;
    }
}