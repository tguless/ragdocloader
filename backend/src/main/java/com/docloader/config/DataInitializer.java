package com.docloader.config;

import com.docloader.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Bean
    @Profile({"dev", "test"})
    public CommandLineRunner initTestData() {
        return args -> {
            fixTestUserPasswords();
        };
    }
    
    @Transactional
    public void fixTestUserPasswords() {
        try {
            // Fix password for sysadmin
            userRepository.findByUsername("sysadmin").ifPresent(user -> {
                if (!user.getPasswordHash().startsWith("$2a$")) {
                    log.info("Encrypting password for test user: sysadmin");
                    String plainPassword = user.getPasswordHash();
                    user.setPasswordHash(passwordEncoder.encode(plainPassword));
                    userRepository.save(user);
                }
            });
            
            // Fix password for admin
            userRepository.findByUsername("admin").ifPresent(user -> {
                if (!user.getPasswordHash().startsWith("$2a$")) {
                    log.info("Encrypting password for test user: admin");
                    String plainPassword = user.getPasswordHash();
                    user.setPasswordHash(passwordEncoder.encode(plainPassword));
                    userRepository.save(user);
                }
            });
            
        } catch (Exception e) {
            log.error("Error fixing test user passwords: {}", e.getMessage(), e);
        }
    }
} 