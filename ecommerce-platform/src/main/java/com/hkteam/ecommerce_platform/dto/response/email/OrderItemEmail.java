package com.hkteam.ecommerce_platform.dto.response.email;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemEmail {
    private String name;
    private String values;
    private int quantity;
    private BigDecimal price;
    private String imageUrl;
}

