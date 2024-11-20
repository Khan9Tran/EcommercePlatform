package com.hkteam.ecommerce_platform.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;

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

    @NotBlank(message = "NOT_BLANK")
    String name;

    String bio;
    LocalDate dateOfBirth;
    Gender gender;
}
