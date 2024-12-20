package com.hkteam.ecommerce_platform.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Size;

import com.hkteam.ecommerce_platform.enums.Gender;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UserUpdateRequest {
    @Size(min = 2, max = 50, message = "MIN_MAX_INVALID")
    String name;

    @Size(max = 255, message = "MAX_INVALID")
    String bio;

    LocalDate dateOfBirth;
    Gender gender;
}
