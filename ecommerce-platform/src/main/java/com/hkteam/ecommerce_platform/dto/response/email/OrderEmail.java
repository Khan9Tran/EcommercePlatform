package com.hkteam.ecommerce_platform.dto.response.email;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderEmail {
    private String id;
    private LocalDateTime orderDate;
    private String seller;
    private List<OrderItemEmail> items;
    private BigDecimal subtotal; // Tổng tiền hàng
    private BigDecimal shopDiscount; // Giảm giá từ shop
    private BigDecimal shippingFee; // Phí vận chuyển
    private BigDecimal total; // Tổng thanh toán
    private String paymentMethod; // Phương thức thanh toán (VNPAY hoặc COD)
    private String paymentStatusUrl; // Link kiểm tra trạng thái thanh toán (nếu có)

    // Constructors, getters, and setters
}
