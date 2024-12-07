package com.hkteam.ecommerce_platform.dto.response.email;

import java.math.BigDecimal;

import lombok.*;
import lombok.experimental.FieldDefaults;

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
