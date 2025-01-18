package com.hkteam.ecommerce_platform.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
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
public class OrderDetailSellerResponse {
    String id;
    String currentStatus;
    Instant createdAt;
    BigDecimal total;
    BigDecimal discount;
    String userAccountName;
    String userEmail;
    String userPhone;
    String note;

    List<OrderItemGetOneSellerResponse> orderItems;
}
