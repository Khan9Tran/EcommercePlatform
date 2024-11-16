package com.hkteam.ecommerce_platform.configuration;

import java.util.HashSet;

import com.hkteam.ecommerce_platform.entity.status.OrderStatus;
import com.hkteam.ecommerce_platform.entity.status.TransactionStatus;
import com.hkteam.ecommerce_platform.enums.OrderStatusName;
import com.hkteam.ecommerce_platform.enums.TransactionStatusName;
import com.hkteam.ecommerce_platform.repository.OrderStatusRepository;
import com.hkteam.ecommerce_platform.repository.TransactionStatusRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
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
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository, TransactionStatusRepository transactionStatusRepository, OrderStatusRepository orderStatusRepository) {
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

            if (roleRepository.findByName(RoleName.SELLER).isEmpty()) {
                log.info("Creating seller role");
                Role adminRole = Role.builder()
                        .name(RoleName.SELLER)
                        .description("Default seller role, please config")
                        .build();

                try {
                    roleRepository.save(adminRole);
                } catch (Exception e) {
                    log.error("Error creating seller role", e);
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

            if (transactionStatusRepository.findById(TransactionStatusName.WAITING.name()).isEmpty()) {
                log.info("Creating transaction pending status");
                TransactionStatus transactionStatus = TransactionStatus.builder()
                        .name(TransactionStatusName.WAITING.name())
                        .build();
                try {
                    transactionStatusRepository.save(transactionStatus);
                } catch (Exception e) {
                    log.error("Error creating pending transaction status", e);
                }
            }

            if (transactionStatusRepository.findById(TransactionStatusName.SUCCESS.name()).isEmpty()) {
                log.info("Creating transaction success status");
                TransactionStatus transactionStatus = TransactionStatus.builder()
                        .name(TransactionStatusName.SUCCESS.name())
                        .build();
                try {
                    transactionStatusRepository.save(transactionStatus);
                } catch (Exception e) {
                    log.error("Error creating success status", e);
                }
            }

            if (orderStatusRepository.findById(OrderStatusName.PENDING.name()).isEmpty()) {
                log.info("Creating pending status");
                OrderStatus orderStatus = OrderStatus.builder()
                        .name(OrderStatusName.PENDING.name())
                        .build();
                try {
                    orderStatusRepository.save(orderStatus);
                } catch (Exception e) {
                    log.error("Error creating pending status", e);
                }
            }

            if (orderStatusRepository.findById(OrderStatusName.ON_HOLD.name()).isEmpty()) {
                log.info("Creating on hold status");
                OrderStatus orderStatus = OrderStatus.builder()
                        .name(OrderStatusName.ON_HOLD.name())
                        .build();
                try {
                    orderStatusRepository.save(orderStatus);
                } catch (Exception e) {
                    log.error("Error creating on hold status", e);
                }
            }
        };
    }
}
