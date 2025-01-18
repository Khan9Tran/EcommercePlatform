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
public class OrderDetailAdminResponse {
    String id;
    String currentStatus;
    String currentStatusTransaction;
    String paymentMethod;
    Instant createdAt;
    String storeId;
    String avatarStore;
    String storeName;
    Float ratingStore;
    Instant lastUpdatedAt;
    BigDecimal total;
    BigDecimal shippingFee;
    BigDecimal discount;
    BigDecimal shippingDiscount;
    BigDecimal grandTotal;
    String storeAccountName;
    String storePhone;
    String storeDetailLocate;
    String storeDetailAddress;
    String storeSubDistrict;
    String storeDistrict;
    String storeProvince;
    String userAccountName;
    String userEmail;
    String userPhone;
    String recipientName;
    String orderPhone;
    String detailLocate;
    String detailAddress;
    String subDistrict;
    String district;
    String province;
    String note;

    List<OrderItemGetOneAdminResponse> orderItems;
}
