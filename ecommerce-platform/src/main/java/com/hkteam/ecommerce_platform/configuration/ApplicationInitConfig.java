package com.hkteam.ecommerce_platform.configuration;

import java.util.HashSet;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hkteam.ecommerce_platform.entity.authorization.Permission;
import com.hkteam.ecommerce_platform.entity.authorization.Role;
import com.hkteam.ecommerce_platform.entity.status.OrderStatus;
import com.hkteam.ecommerce_platform.entity.status.TransactionStatus;
import com.hkteam.ecommerce_platform.entity.user.User;
import com.hkteam.ecommerce_platform.enums.Gender;
import com.hkteam.ecommerce_platform.enums.OrderStatusName;
import com.hkteam.ecommerce_platform.enums.RoleName;
import com.hkteam.ecommerce_platform.enums.TransactionStatusName;
import com.hkteam.ecommerce_platform.repository.*;

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
    ApplicationRunner applicationRunner(
            UserRepository userRepository,
            RoleRepository roleRepository,
            TransactionStatusRepository transactionStatusRepository,
            OrderStatusRepository orderStatusRepository,
            PermissionRepository permissionRepository) {
        return args -> {
            if (roleRepository.findByName(RoleName.USER).isEmpty()) {
                log.info("Creating user role");
                Role userRole = Role.builder()
                        .name(RoleName.USER)
                        .description("Default user role, please config")
                        .build();

                try {
                    roleRepository.save(userRole);

                    var permission = new HashSet<String>();
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

            if (userRepository.findByUsername("ADMIN@123").isEmpty()) {
                log.info("Creating admin user");
                User adminUser = User.builder()
                        .username("ADMIN@123")
                        .passwordDigest(passwordEncoder.encode("ADMIN@123"))
                        .bio("Default admin user, please config")
                        .gender(Gender.OTHER)
                        .phone("00000000000")
                        .email("admin123@gmail.com")
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

            if (transactionStatusRepository
                    .findById(TransactionStatusName.WAITING.name())
                    .isEmpty()) {
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

            if (transactionStatusRepository
                    .findById(TransactionStatusName.SUCCESS.name())
                    .isEmpty()) {
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

            if (orderStatusRepository.findById(OrderStatusName.CONFIRMED.name()).isEmpty()) {
                log.info("Creating on hold status");
                OrderStatus orderStatus = OrderStatus.builder()
                        .name(OrderStatusName.CONFIRMED.name())
                        .build();
                try {
                    orderStatusRepository.save(orderStatus);
                } catch (Exception e) {
                    log.error("Error creating on hold status", e);
                }
            }

            if (orderStatusRepository.findById(OrderStatusName.PREPARING.name()).isEmpty()) {
                log.info("Creating on hold status");
                OrderStatus orderStatus = OrderStatus.builder()
                        .name(OrderStatusName.PREPARING.name())
                        .build();
                try {
                    orderStatusRepository.save(orderStatus);
                } catch (Exception e) {
                    log.error("Error creating on hold status", e);
                }
            }

            if (orderStatusRepository
                    .findById(OrderStatusName.WAITING_FOR_SHIPPING.name())
                    .isEmpty()) {
                log.info("Creating on hold status");
                OrderStatus orderStatus = OrderStatus.builder()
                        .name(OrderStatusName.WAITING_FOR_SHIPPING.name())
                        .build();
                try {
                    orderStatusRepository.save(orderStatus);
                } catch (Exception e) {
                    log.error("Error creating on hold status", e);
                }
            }

            if (orderStatusRepository.findById(OrderStatusName.PICKED_UP.name()).isEmpty()) {
                log.info("Creating on hold status");
                OrderStatus orderStatus = OrderStatus.builder()
                        .name(OrderStatusName.PICKED_UP.name())
                        .build();
                try {
                    orderStatusRepository.save(orderStatus);
                } catch (Exception e) {
                    log.error("Error creating on hold status", e);
                }
            }

            if (orderStatusRepository
                    .findById(OrderStatusName.OUT_FOR_DELIVERY.name())
                    .isEmpty()) {
                log.info("Creating on hold status");
                OrderStatus orderStatus = OrderStatus.builder()
                        .name(OrderStatusName.OUT_FOR_DELIVERY.name())
                        .build();
                try {
                    orderStatusRepository.save(orderStatus);
                } catch (Exception e) {
                    log.error("Error creating on hold status", e);
                }
            }

            if (orderStatusRepository.findById(OrderStatusName.DELIVERED.name()).isEmpty()) {
                log.info("Creating on hold status");
                OrderStatus orderStatus = OrderStatus.builder()
                        .name(OrderStatusName.DELIVERED.name())
                        .build();
                try {
                    orderStatusRepository.save(orderStatus);
                } catch (Exception e) {
                    log.error("Error creating on hold status", e);
                }
            }

            if (orderStatusRepository.findById(OrderStatusName.CANCELLED.name()).isEmpty()) {
                log.info("Creating on hold status");
                OrderStatus orderStatus = OrderStatus.builder()
                        .name(OrderStatusName.CANCELLED.name())
                        .build();
                try {
                    orderStatusRepository.save(orderStatus);
                } catch (Exception e) {
                    log.error("Error creating on hold status", e);
                }
            }

            if (permissionRepository.findById("PERMISSION_PURCHASE").isEmpty()) {

                var permission = Permission.builder()
                        .name("PERMISSION_PURCHASE")
                        .description("Permission to purchase")
                        .build();
                try {
                    permissionRepository.save(permission);
                } catch (Exception e) {
                    log.error("Error creating permission", e);
                }
            }
        };
    }
}
