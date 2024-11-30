package com.hkteam.ecommerce_platform.dto.response;

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
public class OrderStatusHistoryResponseUser {
    String id;
    String orderStatusName;
    String remarks;
    Instant createdAt;
    Instant lastUpdatedAt;
}
