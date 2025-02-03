package com.hkteam.ecommerce_platform.util;

import java.util.Arrays;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class ReviewUtil {
    public String validateSortOrOrder(String value, String[] validValues) {
        return Arrays.asList(validValues).contains(value) ? value : null;
    }

    public Sort validateSortAndOrder(String sortBy, String orderBy, String[] validSortBy, String[] validOrderBy) {
        sortBy = validateSortOrOrder(sortBy, validSortBy);
        orderBy = validateSortOrOrder(orderBy, validOrderBy);
        return (sortBy == null || orderBy == null)
                ? Sort.unsorted()
                : Sort.by(Sort.Direction.fromString(orderBy), sortBy);
    }
}
