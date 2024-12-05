package com.hkteam.ecommerce_platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class RatingCountResponse {
    long fiveStar;
    long fourStar;
    long threeStar;
    long twoStar;
    long oneStar;
}
