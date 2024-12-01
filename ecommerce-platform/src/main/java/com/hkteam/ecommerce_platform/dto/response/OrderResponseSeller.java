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
public class OrderResponseSeller {
    String id;
    BigDecimal total;
    BigDecimal discount;
    String note;
    Instant createdAt;
    String currentStatus;
    String accountName;
    String userEmail;
    String userPhone;

    List<OrderItemResponseSeller> orderItems;
}
