package com.hkteam.ecommerce_platform.dto.response;

import java.time.LocalDate;
import java.util.List;

import com.hkteam.ecommerce_platform.enums.Gender;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateResponse {
    String id;
    String name;
    Gender gender;
    String bio;
    String phone;
    LocalDate dateOfBirth;
    List<RoleResponse> roles;
    List<ExternalAuthResponse> externalAuth;
}
