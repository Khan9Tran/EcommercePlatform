package com.hkteam.ecommerce_platform.util;

import java.math.BigDecimal;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ShippingFeeUtil {
    public BigDecimal calculateShippingFee() {
        return new BigDecimal(24000);
    }
}
