package com.hkteam.ecommerce_platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

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
public class AddressUpdateRequest {
    @NotBlank(message = "NOT_BLANK")
    String recipientName;

    @Pattern(regexp = "^0\\d{9}$", message = "INVALID_PHONE")
    String phone;

    @NotBlank(message = "NOT_BLANK")
    String province;

    @NotBlank(message = "NOT_BLANK")
    String district;

    @NotBlank(message = "NOT_BLANK")
    String subDistrict;

    String detailLocate;

    @NotBlank(message = "NOT_BLANK")
    String detailAddress;
}
