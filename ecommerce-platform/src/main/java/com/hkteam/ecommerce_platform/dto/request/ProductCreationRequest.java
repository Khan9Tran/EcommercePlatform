package com.hkteam.ecommerce_platform.dto.request;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import com.hkteam.ecommerce_platform.entity.product.Variant;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ProductCreationRequest {

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

    @NonNull
    Long categoryId;

    Set<ComponentOfProductRequest> components;

    List<AttributeHasValuesRequest> attributesHasValues;

    List<VariantOfProductRequest> variantOfProducts;

}
