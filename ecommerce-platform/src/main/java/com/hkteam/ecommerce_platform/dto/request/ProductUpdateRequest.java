package com.hkteam.ecommerce_platform.dto.request;

import com.hkteam.ecommerce_platform.dto.response.BrandOfProductResponse;
import com.hkteam.ecommerce_platform.dto.response.CategoryOfProductResponse;
import com.hkteam.ecommerce_platform.dto.response.StoreOfProductResponse;
import com.hkteam.ecommerce_platform.entity.image.ProductImage;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ProductUpdateRequest {
    @NonNull
    @Size(min = 1, max = 150, message = "PRODUCT_INVALID")
    String name;

    @NonNull
    @Size(min = 1, max = 254, message = "PRODUCT_INVALID")
    String description;

    @Nullable
    @Size(max = 1999, message = "PRODUCT_INVALID")
    String details;

    @NonNull
    boolean isAvailable;

    @NonNull
    @Min(value = 0)
    int quantity;

    @NonNull
    @Min(value = 0, message = "PRICE_INVALID")
    BigDecimal originalPrice;

    @NonNull
    @Min(value = 0, message = "PRICE_INVALID")
    BigDecimal salePrice;

    @NonNull
    Long brandId;
}
