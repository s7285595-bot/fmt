package com.findmytutor.findmytutor_backend.controller;

import java.util.Comparator;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.findmytutor.findmytutor_backend.dto.NearbyTutorResponse;
import com.findmytutor.findmytutor_backend.dto.TutorResponse;
import com.findmytutor.findmytutor_backend.model.Tutor;
import com.findmytutor.findmytutor_backend.model.User;
import com.findmytutor.findmytutor_backend.repository.TutorRepository;
import com.findmytutor.findmytutor_backend.repository.UserRepository;

@RestController
@RequestMapping("/api/tutors")
public class TutorController {

    private final TutorRepository tutorRepository;
    private final UserRepository userRepository;

    public TutorController(
            TutorRepository tutorRepository,
            UserRepository userRepository) {

        this.tutorRepository = tutorRepository;
        this.userRepository = userRepository;
    }

    // CREATE TUTOR PROFILE
    @PostMapping("/profile")
    public ResponseEntity<?> createProfile(
            @RequestBody Tutor tutor,
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (!"TUTOR".equals(user.getRole())) {
            return ResponseEntity.status(403)
                    .body("Only tutors can create a tutor profile");
        }

        if (tutorRepository.findByUserId(user.getId()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body("Tutor profile already exists");
        }

        tutor.setUser(user);
        tutor.setRating(0.0);

        Tutor savedTutor = tutorRepository.save(tutor);

        return ResponseEntity.ok(
                new TutorResponse(savedTutor)
        );
    }

    // GET MY TUTOR PROFILE
    @GetMapping("/profile")
    public ResponseEntity<?> getMyProfile(
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return tutorRepository.findByUserId(user.getId())
                .map(tutor ->
                        ResponseEntity.ok(
                                new TutorResponse(tutor)
                        )
                )
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }
@GetMapping
public ResponseEntity<?> getAllTutors(
        @RequestParam(required = false) String subject,
        @RequestParam(required = false) String city,
        @RequestParam(required = false) String teachingMode,
        @RequestParam(required = false) Double minFee,
@RequestParam(required = false) Double maxFee
    ) {

  List<Tutor> tutors = tutorRepository.findAll()
        .stream()
        .filter(tutor ->
                "APPROVED".equals(tutor.getUser().getStatus())
        )
        .toList();

    if (subject != null && !subject.isBlank()) {
        tutors = tutors.stream()
                .filter(tutor ->
                        tutor.getSubjects() != null &&
                        tutor.getSubjects()
                                .toLowerCase()
                                .contains(subject.toLowerCase()))
                .toList();
    }

    if (city != null && !city.isBlank()) {
        tutors = tutors.stream()
                .filter(tutor ->
                        tutor.getCity() != null &&
                        tutor.getCity()
                                .equalsIgnoreCase(city))
                .toList();
    }

    if (teachingMode != null && !teachingMode.isBlank()) {
        tutors = tutors.stream()
                .filter(tutor ->
                        tutor.getTeachingMode() != null &&
                        tutor.getTeachingMode()
                                .equalsIgnoreCase(teachingMode))
                .toList();
    }

    if (minFee != null) {
    tutors = tutors.stream()
            .filter(tutor ->
                    tutor.getHourlyFee() != null &&
                    tutor.getHourlyFee() >= minFee)
            .toList();
}

if (maxFee != null) {
    tutors = tutors.stream()
            .filter(tutor ->
                    tutor.getHourlyFee() != null &&
                    tutor.getHourlyFee() <= maxFee)
            .toList();
}

    return ResponseEntity.ok(
            tutors.stream()
                    .map(TutorResponse::new)
                    .toList()
    );
}




    // UPDATE MY TUTOR PROFILE
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestBody Tutor updatedTutor,
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (!"TUTOR".equals(user.getRole())) {
            return ResponseEntity.status(403)
                    .body("Only tutors can update a tutor profile");
        }

        Tutor existingTutor = tutorRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Tutor profile not found"));

        existingTutor.setQualification(
                updatedTutor.getQualification()
        );

        existingTutor.setExperience(
                updatedTutor.getExperience()
        );

        existingTutor.setSubjects(
                updatedTutor.getSubjects()
        );

        existingTutor.setHourlyFee(
                updatedTutor.getHourlyFee()
        );

        existingTutor.setCity(
                updatedTutor.getCity()
        );

        existingTutor.setTeachingMode(
                updatedTutor.getTeachingMode()
        );

        existingTutor.setBio(
                updatedTutor.getBio()
        );
        existingTutor.setLatitude(
        updatedTutor.getLatitude()
);

existingTutor.setLongitude(
        updatedTutor.getLongitude()
);

        Tutor savedTutor = tutorRepository.save(existingTutor);

        return ResponseEntity.ok(
                new TutorResponse(savedTutor)
        );
    }


// GET NEARBY TUTORS
@GetMapping("/nearby")
public ResponseEntity<?> getNearbyTutors(
        @RequestParam double latitude,
        @RequestParam double longitude,
        @RequestParam(defaultValue = "5") double radius) {

  List<NearbyTutorResponse> nearbyTutors = tutorRepository.findAll()
        .stream()
        .filter(tutor ->
                "APPROVED".equals(tutor.getUser().getStatus())
        )
        .filter(tutor ->
                tutor.getLatitude() != null &&
                tutor.getLongitude() != null)
            .map(tutor -> {

                double distance = calculateDistance(
                        latitude,
                        longitude,
                        tutor.getLatitude(),
                        tutor.getLongitude()
                );

                return new NearbyTutorResponse(
                        tutor.getId(),
                        tutor.getUser().getName(),
                        tutor.getUser().getEmail(),
                        tutor.getQualification(),
                        tutor.getExperience(),
                        tutor.getSubjects(),
                        tutor.getHourlyFee(),
                        tutor.getCity(),
                        tutor.getTeachingMode(),
                        tutor.getBio(),
                        tutor.getRating(),
                        Math.round(distance * 100.0) / 100.0
                );
            })
            .filter(tutor ->
                    tutor.getDistanceKm() <= radius)
            .sorted(Comparator.comparing(
                    NearbyTutorResponse::getDistanceKm))
            .toList();

    return ResponseEntity.ok(nearbyTutors);
}

@GetMapping("/{id:\\d+}")
public ResponseEntity<?> getTutorById(
        @PathVariable Long id) {

    Tutor tutor = tutorRepository.findById(id)
            .orElse(null);

    if (tutor == null) {
        return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(
            new TutorResponse(tutor)
    );
}
private double calculateDistance(
        double latitude1,
        double longitude1,
        double latitude2,
        double longitude2) {

    final int EARTH_RADIUS_KM = 6371;

    double latDistance = Math.toRadians(latitude2 - latitude1);
    double lonDistance = Math.toRadians(longitude2 - longitude1);

    double a =
            Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
            + Math.cos(Math.toRadians(latitude1))
            * Math.cos(Math.toRadians(latitude2))
            * Math.sin(lonDistance / 2)
            * Math.sin(lonDistance / 2);

    double c = 2 * Math.atan2(
            Math.sqrt(a),
            Math.sqrt(1 - a)
    );

    return EARTH_RADIUS_KM * c;
}
    
}