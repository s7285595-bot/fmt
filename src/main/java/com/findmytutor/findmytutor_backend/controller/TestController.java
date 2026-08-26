package com.findmytutor.findmytutor_backend.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/")
    public String home() {
        return "FindMyTutor Backend is running!";
    }

    @GetMapping("/api/test")
    public String protectedTest(Authentication authentication) {
        return "Hello " + authentication.getName()
                + ", you are authenticated!";
    }
}