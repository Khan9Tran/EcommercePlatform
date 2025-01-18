package com.hkteam.ecommerce_platform.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class OrderItemGetOneAdminResponse {
    Long id;
    String productName;
    String productMainImageUrl;
    String productSlug;
    String productNameBrand;
    List<String> values;
    int quantity;
    BigDecimal price;
}
