package com.jred.wordclub.config;

import com.jred.wordclub.entity.User;
import com.jred.wordclub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the admin user on first startup if it doesn't exist.
 * Admin password is read from ADMIN_PASSWORD env var (default: 123123).
 * In production, always set ADMIN_PASSWORD to a strong value.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${ADMIN_PASSWORD:123123}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("root").isEmpty()) {
            User admin = new User();
            admin.setUsername("root");
            admin.setEmail("admin@wordclub.local");
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setNickname("Administrator");
            admin.setEnabled(true);
            admin.setRole("ADMIN");
            userRepository.save(admin);
            log.info("Admin user (root) created successfully");
        }
    }
}
