package com.hkteam.ecommerce_platform.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class StoreStatisticsResponse {
    long numberOfOrdersConfirmed;
    long numberOfOrdersPreparing;
    long numberOfOrdersWaitingForShipping;
    long numberOfOrdersCancelled;

    long numberOfProductsTemporarilyBlocked;
    long numberOfProductsOutOfStock;

    List<StoreSalesLastSevenDay> storeSalesLastSevenDays;

    BigDecimal dailyRevenue;
    long numberOfOrdersDelivered;
    long numberOfOrdersPending;
}
