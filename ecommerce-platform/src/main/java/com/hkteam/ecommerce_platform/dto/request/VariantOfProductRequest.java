package com.hkteam.ecommerce_platform.dto.request;

import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

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
    BigDecimal originalPrice;
    @NonNull
    int quantity;
    @NonNull
    @Min(value = 0, message = "PRICE_INVALID")
    BigDecimal salePrice;
}
