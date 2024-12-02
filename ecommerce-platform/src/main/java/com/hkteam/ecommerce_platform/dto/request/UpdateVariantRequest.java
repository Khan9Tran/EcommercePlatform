package com.hkteam.ecommerce_platform.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.Max;
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
    @Max(value = 999999999, message = "PRICE_INVALID")
    BigDecimal originalPrice;

    @NonNull
    @Min(value = 0, message = "PRICE_INVALID")
    @Max(value = 999999999, message = "PRICE_INVALID")
    BigDecimal salePrice;

    @NonNull
    @Min(value = 0)
    @Max(value = 999)
    int quantity;

    @NonNull
    boolean isAvailable;
}
