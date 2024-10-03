package com.hkteam.ecommerce_platform.dto.request;

import jakarta.validation.constraints.Pattern;

import com.hkteam.ecommerce_platform.validator.ValidSpace;

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
    @ValidSpace()
    String recipientName;

    @Pattern(regexp = "^0\\d{9}$", message = "INVALID_PHONE")
    String phone;

    @ValidSpace
    String province;

    @ValidSpace
    String district;

    @ValidSpace
    String detailAddress;

    @ValidSpace
    String detailLocate;
}
