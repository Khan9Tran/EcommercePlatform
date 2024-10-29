package com.hkteam.ecommerce_platform.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UpdateVariantRequest {

    @NonNull
    @Min(value = 0, message = "PRICE_INVALID")
    BigDecimal originalPrice;

    @NonNull
    @Min(value = 0, message = "PRICE_INVALID")
    BigDecimal salePrice;

    @NonNull
    @Min(value = 0)
    int quantity;

    @NonNull
    boolean isAvailable;
}
