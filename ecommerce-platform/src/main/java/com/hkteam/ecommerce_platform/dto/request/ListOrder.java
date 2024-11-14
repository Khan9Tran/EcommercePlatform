package com.hkteam.ecommerce_platform.dto.request;

import java.util.List;

import com.hkteam.ecommerce_platform.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ListOrder {
    Long addressId;
    PaymentMethod paymentMethod;
    List<OrderRequest> orders;
    String note;
}
