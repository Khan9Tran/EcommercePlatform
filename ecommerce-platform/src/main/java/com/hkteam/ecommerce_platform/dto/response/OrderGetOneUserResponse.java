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
public class OrderGetOneUserResponse {
    String id;
    String currentStatus;
    String storeId;
    String recipientName;
    String orderPhone;
    String defaultAddressStr;
    String avatarStore;
    String storeName;
    Float ratingStore;
    Instant lastUpdatedAt;
    BigDecimal total;
    BigDecimal shippingFee;
    BigDecimal shippingDiscount;
    BigDecimal discount;
    BigDecimal grandTotal;
    String paymentMethod;

    List<OrderItemGetOneUserResponse> orderItems;
    List<OrderStatusHistoryGetOneUserResponse> orderStatusHistories;
}
