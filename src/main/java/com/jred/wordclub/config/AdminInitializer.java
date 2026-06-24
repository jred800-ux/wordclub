package com.jred.wordclub.config;

import com.jred.wordclub.entity.User;
import com.jred.wordclub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("root").isEmpty()) {
            User admin = new User();
            admin.setUsername("root");
            admin.setEmail("admin@wordclub.local");
            admin.setPassword(passwordEncoder.encode("123123"));
            admin.setNickname("Administrator");
            admin.setEnabled(true);
            admin.setRole("ADMIN");
            userRepository.save(admin);
            log.info("Admin user (root) created successfully");
        }
    }
}
