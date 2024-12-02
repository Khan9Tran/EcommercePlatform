package com.hkteam.ecommerce_platform.dto.request;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class VariantOfProductRequest {
    @NonNull
    List<String> values;

    @NonNull
    boolean isAvailable;

    @NonNull
    @Min(value = 0, message = "PRICE_INVALID")
    @Max(value = 999999999, message = "PRICE_INVALID")
    BigDecimal originalPrice;

    @NonNull
    @Min(value = 0)
    @Max(value = 999)
    int quantity;

    @NonNull
    @Min(value = 0, message = "PRICE_INVALID")
    @Max(value = 999999999, message = "PRICE_INVALID")
    BigDecimal salePrice;
}
