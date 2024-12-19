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
public class AdminStatisticsResponse {
    long numberOfOrdersWaitingForShipping;
    long numberOfOrdersPickedUp;
    long numberOfOrdersOutForDelivery;
    long numberOfOrdersCancelled;
    long numberOfOrdersDelivered;
    long numberOfOrdersPending;

    BigDecimal dailyRevenue;
    BigDecimal weeklyRevenue;
    BigDecimal monthlyRevenue;
    BigDecimal yearlyRevenue;
    BigDecimal revenueIncreaseCompareYesterday;
    BigDecimal revenueIncreaseCompareLastWeek;
    BigDecimal revenueIncreaseCompareLastMonth;
    BigDecimal revenueIncreaseCompareLastYear;
    int dailyRevenueGrowthRate;
    int weeklyRevenueGrowthRate;
    int monthlyRevenueGrowthRate;
    int yearlyRevenueGrowthRate;

    long totalNumberOfCustomers;
    long totalNumberOfSellers;
    long totalNumberOfAdmins;

    long dailyNumberOfCustomer;
    long weeklyNumberOfCustomer;
    long monthlyNumberOfCustomer;
    long yearlyNumberOfCustomer;
    long numberOfCustomersIncreaseCompareYesterday;
    long numberOfCustomersIncreaseCompareLastWeek;
    long numberOfCustomersIncreaseCompareLastMonth;
    long numberOfCustomersIncreaseCompareLastYear;
    int dailyCustomerGrowthRate;
    int weeklyCustomerGrowthRate;
    int monthlyCustomerGrowthRate;
    int yearlyCustomerGrowthRate;

    long dailyNumberOfSeller;
    long weeklyNumberOfSeller;
    long monthlyNumberOfSeller;
    long yearlyNumberOfSeller;
    long numberOfSellersIncreaseCompareYesterday;
    long numberOfSellersIncreaseCompareLastWeek;
    long numberOfSellersIncreaseCompareLastMonth;
    long numberOfSellersIncreaseCompareLastYear;
    int dailySellerGrowthRate;
    int weeklySellerGrowthRate;
    int monthlySellerGrowthRate;
    int yearlySellerGrowthRate;

    List<StoreRevenueResponse> top5StoresByRevenue;
}
