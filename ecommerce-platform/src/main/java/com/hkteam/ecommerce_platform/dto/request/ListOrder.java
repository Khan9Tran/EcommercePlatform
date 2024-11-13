package com.hkteam.ecommerce_platform.dto.request;

import java.util.List;

import com.hkteam.ecommerce_platform.enums.PaymentMethod;

public class ListOrder {
    Long addressId;
    PaymentMethod paymentMethod;
    List<OrderRequest> orders;
}
