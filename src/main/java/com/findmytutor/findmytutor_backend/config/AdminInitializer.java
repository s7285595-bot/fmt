package com.findmytutor.findmytutor_backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.findmytutor.findmytutor_backend.model.User;
import com.findmytutor.findmytutor_backend.repository.UserRepository;

@Configuration
public class AdminInitializer {

    @Bean
    CommandLineRunner createAdmin(
            UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder) {

        return args -> {

            if (userRepository.findByEmail("admin@findmytutor.com").isEmpty()) {

                User admin = new User();

                admin.setName("Admin");
                admin.setEmail("admin@findmytutor.com");
                admin.setPassword(
                        passwordEncoder.encode("Admin@123")
                );
                admin.setRole("ADMIN");
                admin.setStatus("ACTIVE");

                userRepository.save(admin);

                System.out.println("=================================");
                System.out.println("ADMIN CREATED");
                System.out.println("Email: admin@findmytutor.com");
                System.out.println("Password: Admin@123");
                System.out.println("=================================");
            }
        };
    }
}