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
public class OrderResponseAdmin {
    String id;
    BigDecimal total;
    BigDecimal discount;
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
    Instant createdAt;
    String currentStatus;
    String defaultAddressStr;
    String accountName;
    String userName;
    String userEmail;
    String userPhone;
    String orderPhone;
    String currentStatusTransaction;
    String paymentMethod;

    List<OrderItemResponseAdmin> orderItems;
}
