package com.hkteam.ecommerce_platform.util;

import java.util.UUID;

import com.hkteam.ecommerce_platform.enums.TypeSlug;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SlugUtils {
    public static String getSlug(String input, TypeSlug typeSlug) {
        String slug = input.trim()
                .replaceAll("[^\\p{L}\\p{N}\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-$|-$", "");

        String code = generateNumericCode();
        return slug + "-" + typeSlug.name().substring(0, 3).toLowerCase() + "." + code;
    }

    private static String generateNumericCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
