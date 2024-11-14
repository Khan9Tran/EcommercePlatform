package com.hkteam.ecommerce_platform.util;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

@UtilityClass
public class ShippingFeeUtil {
    public BigDecimal calculateShippingFee() {
        return new BigDecimal(24000);
    }
}
