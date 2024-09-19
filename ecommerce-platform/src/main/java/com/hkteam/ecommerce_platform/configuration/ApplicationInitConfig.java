package com.hkteam.ecommerce_platform.configuration;

import java.util.HashSet;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hkteam.ecommerce_platform.entity.authorization.Role;
import com.hkteam.ecommerce_platform.entity.user.User;
import com.hkteam.ecommerce_platform.enums.Gender;
import com.hkteam.ecommerce_platform.enums.RoleName;
import com.hkteam.ecommerce_platform.repository.RoleRepository;
import com.hkteam.ecommerce_platform.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {
        return args -> {
            if (roleRepository.findByName(RoleName.USER).isEmpty()) {
                log.info("Creating user role");
                Role userRole = Role.builder()
                        .name(RoleName.USER)
                        .description("Default user role, please config")
                        .build();

                try {
                    roleRepository.save(userRole);
                } catch (Exception e) {
                    log.error("Error creating user role", e);
                }
            }
            if (roleRepository.findByName(RoleName.ADMIN).isEmpty()) {
                log.info("Creating admin role");
                Role adminRole = Role.builder()
                        .name(RoleName.ADMIN)
                        .description("Default admin role, please config")
                        .build();

                try {
                    roleRepository.save(adminRole);
                } catch (Exception e) {
                    log.error("Error creating admin role", e);
                }
            }

            if (userRepository.findByUsername(RoleName.ADMIN.name()).isEmpty()) {
                log.info("Creating admin user");
                User adminUser = User.builder()
                        .username(RoleName.ADMIN.name())
                        .passwordDigest(passwordEncoder.encode(RoleName.ADMIN.name()))
                        .bio("Default admin user, please config")
                        .gender(Gender.OTHER)
                        .phone("00000000000")
                        .email("")
                        .roles(new HashSet<>(roleRepository
                                .findByName(RoleName.ADMIN)
                                .map(role -> {
                                    HashSet<Role> roles = new HashSet<>();
                                    roles.add(role);
                                    return roles;
                                })
                                .orElse(new HashSet<>())))
                        .build();

                try {
                    userRepository.save(adminUser);
                } catch (Exception e) {
                    log.error("Error creating admin user", e);
                }
            }
        };
    }
}
