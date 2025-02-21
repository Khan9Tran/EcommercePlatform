package com.hkteam.ecommerce_platform.entity.embed;

import jakarta.persistence.Embeddable;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Embeddable
public class ViewProductKey {
    String userId;
    String productId;
}
