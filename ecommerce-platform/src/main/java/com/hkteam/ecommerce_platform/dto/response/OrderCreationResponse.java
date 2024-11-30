package com.hkteam.ecommerce_platform.dto.response;

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
public class OrderCreationResponse {
    String paymentUrl;
    String status;
    String paymentId;
}
