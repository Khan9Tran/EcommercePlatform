package com.hkteam.ecommerce_platform.dto.request;

import jakarta.validation.constraints.Min;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class CartItemUpdateQuantityRequest {
    @NonNull
    @Min(value = 1, message = "MIN value is 1")
    int quantity;
}
