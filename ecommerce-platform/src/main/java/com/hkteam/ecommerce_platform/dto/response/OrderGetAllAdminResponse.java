package com.hkteam.ecommerce_platform.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

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
public class OrderGetAllAdminResponse {
    String id;
    Instant createdAt;
    String phone;
    String province;
    String currentStatusTransaction;
    String paymentMethod;
    String currentStatus;
    BigDecimal grandTotal;
}
