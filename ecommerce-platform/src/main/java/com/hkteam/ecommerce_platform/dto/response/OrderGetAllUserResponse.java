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
public class OrderGetAllUserResponse {
    String id;
    String storeId;
    String avatarStore;
    String storeName;
    Float ratingStore;
    String currentStatus;
    Instant lastUpdatedAt;
    String paymentMethod;
    String currentStatusTransaction;
    BigDecimal grandTotal;
    String note;

    List<OrderItemGetAllUserResponse> orderItems;
}
