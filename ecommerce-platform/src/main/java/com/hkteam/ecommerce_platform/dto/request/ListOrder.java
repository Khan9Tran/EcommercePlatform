package com.hkteam.ecommerce_platform.dto.request;

import com.hkteam.ecommerce_platform.enums.PaymentMethod;

import java.util.List;

public class ListOrder {
    Long addressId;
    PaymentMethod paymentMethod;
    List<OrderRequest> orders;
}
