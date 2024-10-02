package com.hkteam.ecommerce_platform.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtils {
    public String convertEmptyToNull(String str) {
        return (str != null && str.isEmpty() ? null : str);
    }
}
