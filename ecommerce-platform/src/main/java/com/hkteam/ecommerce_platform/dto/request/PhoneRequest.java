package com.hkteam.ecommerce_platform.dto.request;

import jakarta.validation.constraints.Pattern;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class PhoneRequest {
    @NonNull
    String userId;

    @Pattern(regexp = "^0\\d{9}$", message = "INVALID_PHONE")
    String phone;
}
