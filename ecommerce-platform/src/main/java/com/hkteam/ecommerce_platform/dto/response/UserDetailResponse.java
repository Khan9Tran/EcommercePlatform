package com.hkteam.ecommerce_platform.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import com.hkteam.ecommerce_platform.enums.Gender;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDetailResponse {
    String username;
    String id;
    String name;
    String bio;
    String email;
    String phone;
    Gender gender;
    LocalDate dateOfBirth;
    String imageUrl;
    String emailValidationStatus;
    String phoneValidationStatus;
    Boolean noPassword;
    boolean isBlocked;
    Instant createdAt;
    List<RoleResponse> roles;
}
