package com.hkteam.ecommerce_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatisticResponse {
    Integer totalCount;
    BigDecimal totalAmount;
    List<StatisticItem> data;
    Integer totalItems; //tự phân trang nha cha, tự build đi
}


