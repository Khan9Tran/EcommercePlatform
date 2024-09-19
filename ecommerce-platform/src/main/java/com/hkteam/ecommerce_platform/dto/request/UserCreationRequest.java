package com.hkteam.ecommerce_platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UserCreationRequest {

    @NotBlank(message = "NOT_BLANK")
    @Size(min = 6, max = 20, message = "USERNAME_INVALID")
    String username;

    @NotBlank(message = "NOT_BLANK")
    String firstName;

    @NotBlank(message = "NOT_BLANK")
    String lastName;

    @NotBlank(message = "NOT_BLANK")
    @Size(min = 8, max = 20, message = "PASSWORD_INVALID")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "PASSWORD_FORMAT_INVALID")
    String password;

    String passwordConfirmation;
}
