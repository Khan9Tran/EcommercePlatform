package com.hkteam.ecommerce_platform.constant;

import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(makeFinal = true)
@NoArgsConstructor
public class TokenPurpose {
    public static String RESET_PASSWORD = "RESET_PASSWORD";
    public static String EMAIL = "EMAIL";
}
