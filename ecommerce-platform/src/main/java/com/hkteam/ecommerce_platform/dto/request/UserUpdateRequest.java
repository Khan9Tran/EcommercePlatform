package com.hkteam.ecommerce_platform.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    @NotBlank(message = "NOT_BLANK")
    String firstName;

    @NotBlank(message = "NOT_BLANK")
    String lastName;

    Gender gender;

    String bio;

    @Pattern(regexp = "^0\\d{9}$", message = "PHONE_START_0")
    @Size(min = 10, max = 10, message = "PHONE_10_DIGITS")
    String phone;

    LocalDate dateOfBirth;
}
