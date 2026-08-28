package com.findmytutor.findmytutor_backend.dto;

public class NearbyTutorResponse {

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

    public NearbyTutorResponse(
            Long id,
            String name,
            String email,
            String qualification,
            Integer experience,
            String subjects,
            Double hourlyFee,
            String city,
            String teachingMode,
            String bio,
            Double rating,
            Double distanceKm) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.qualification = qualification;
        this.experience = experience;
        this.subjects = subjects;
        this.hourlyFee = hourlyFee;
        this.city = city;
        this.teachingMode = teachingMode;
        this.bio = bio;
        this.rating = rating;
        this.distanceKm = distanceKm;
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

    public Double getDistanceKm() {
        return distanceKm;
    }
}