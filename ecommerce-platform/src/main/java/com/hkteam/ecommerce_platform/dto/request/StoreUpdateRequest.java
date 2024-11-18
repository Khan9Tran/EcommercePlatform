package com.hkteam.ecommerce_platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
public class StoreUpdateRequest {

    @NotBlank(message = "NOT_BLANK")
    String name;

    String bio;

    @NotNull(message = "NOT_NULL")
    Long defaultAddressId;
}
