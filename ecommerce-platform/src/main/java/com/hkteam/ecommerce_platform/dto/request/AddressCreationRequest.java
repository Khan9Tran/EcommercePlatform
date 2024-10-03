package com.hkteam.ecommerce_platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

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
public class AddressCreationRequest {
    @NotBlank(message = "NOT_BLANK")
    String recipientName;

    @Pattern(regexp = "^0\\d{9}$", message = "PHONE_START_0")
    @Size(min = 10, max = 10, message = "PHONE_10_DIGITS")
    String phone;

    @NotBlank(message = "NOT_BLANK")
    String province;

    @NotBlank(message = "NOT_BLANK")
    String district;

    @NotBlank(message = "NOT_BLANK")
    String detailAddress;

    String detailLocate;
}
