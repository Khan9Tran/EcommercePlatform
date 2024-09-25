package com.hkteam.ecommerce_platform.util;

public class StringUtils {
    public static String convertEmptyToNull(String str) {
        return (str != null && str.isEmpty() ? null : str);
    }
}
