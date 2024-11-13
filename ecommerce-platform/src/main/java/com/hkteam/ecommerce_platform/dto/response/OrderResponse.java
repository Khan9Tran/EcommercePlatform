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
public class OrderResponse {
    String id;
    BigDecimal total;
    BigDecimal discount;
    String phone;
    String recipientName;
    String province;
    String district;
    String subDistrict;
    String detailAddress;
    String detailLocate;
    BigDecimal shippingFee;
    BigDecimal shippingDiscount;
    BigDecimal shippingTotal;
    BigDecimal grandTotal;
    BigDecimal promo;
    String note;
    String code;
    Instant createdAt;
    Instant lastUpdatedAt;
    String currentStatus;
    String defaultAddressStr;
    String userId;
    String accountName;
    String userName;
    String userEmail;
    String userPhone;
    int totalOrders;

    List<OrderItemResponse> orderItems;
}
