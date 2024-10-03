package com.hkteam.ecommerce_platform.dto.response;

import java.time.Instant;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressResponse {
    Long id;
    String recipientName;
    String phone;
    String province;
    String district;
    String detailAddress;
    String detailLocate;
    Instant createdAt;
    Instant lastUpdatedAt;
    UserResponse user;
}
