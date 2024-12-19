package com.hkteam.ecommerce_platform.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.hkteam.ecommerce_platform.dto.response.AdminStatisticsResponse;
import com.hkteam.ecommerce_platform.dto.response.RevenueOneDayResponse;
import com.hkteam.ecommerce_platform.dto.response.RevenueOneYearResponse;
import com.hkteam.ecommerce_platform.dto.response.StoreRevenueResponse;
import com.hkteam.ecommerce_platform.enums.RoleName;
import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;
import com.hkteam.ecommerce_platform.repository.OrderStatusHistoryRepository;
import com.hkteam.ecommerce_platform.repository.StoreRepository;
import com.hkteam.ecommerce_platform.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminService {
    OrderStatusHistoryRepository oshRepository;
    UserRepository userRepository;
    StoreRepository storeRepository;

    static final String IS_DAY = "isDay";
    static final String IS_WEEK = "isWeek";
    static final String IS_MONTH = "isMonth";
    static final String IS_YEAR = "isYear";

    @PreAuthorize("hasRole('ADMIN')")
    public AdminStatisticsResponse getAdminStatistic() {
        long numberOfOrdersWaitingForShipping = getNumberOfOrdersByStatus("WAITING_FOR_SHIPPING");
        long numberOfOrdersPickedUp = getNumberOfOrdersByStatus("PICKED_UP");
        long numberOfOrdersOutForDelivery = getNumberOfOrdersByStatus("OUT_FOR_DELIVERY");
        long numberOfOrdersCancelled = getNumberOfOrdersByStatus("CANCELLED");
        long numberOfOrdersDelivered = getNumberOfOrdersByStatus("DELIVERED");
        long numberOfOrdersPending = getNumberOfOrdersByStatus("PENDING");

        Instant today = Instant.now();

        BigDecimal dailyRevenue = calculateRevenue(IS_DAY, today);
        BigDecimal weeklyRevenue = calculateRevenue(IS_WEEK, today);
        BigDecimal monthlyRevenue = calculateRevenue(IS_MONTH, today);
        BigDecimal yearlyRevenue = calculateRevenue(IS_YEAR, today);

        BigDecimal yesterdayRevenue = calculateRevenue(IS_DAY, today.minusSeconds(86400));
        BigDecimal lastWeekRevenue = calculateRevenue(IS_WEEK, today.minusSeconds(7L * 86400));
        BigDecimal lastMonthRevenue = calculateRevenue(IS_MONTH, today.minusSeconds(30L * 86400));
        BigDecimal lastYearRevenue = calculateRevenue(IS_YEAR, today.minusSeconds(365L * 86400));

        BigDecimal revenueIncreaseCompareYesterday = calculateRevenueIncrease(dailyRevenue, yesterdayRevenue);
        BigDecimal revenueIncreaseCompareLastWeek = calculateRevenueIncrease(weeklyRevenue, lastWeekRevenue);
        BigDecimal revenueIncreaseCompareLastMonth = calculateRevenueIncrease(monthlyRevenue, lastMonthRevenue);
        BigDecimal revenueIncreaseCompareLastYear = calculateRevenueIncrease(yearlyRevenue, lastYearRevenue);

        int dailyRevenueGrowthRate = calculateRevenueGrowthRate(dailyRevenue, yesterdayRevenue);
        int weeklyRevenueGrowthRate = calculateRevenueGrowthRate(weeklyRevenue, lastWeekRevenue);
        int monthlyRevenueGrowthRate = calculateRevenueGrowthRate(monthlyRevenue, lastMonthRevenue);
        int yearlyRevenueGrowthRate = calculateRevenueGrowthRate(yearlyRevenue, lastYearRevenue);

        long totalNumberOfCustomers = userRepository.countByRolesName(RoleName.USER);
        long totalNumberOfSellers = userRepository.countByRolesName(RoleName.SELLER);
        long totalNumberOfAdmins = userRepository.countByRolesName(RoleName.ADMIN);

        long dailyNumberOfCustomer = countUserByIntervalAndTime(List.of(RoleName.USER), IS_DAY, today);
        long weeklyNumberOfCustomer = countUserByIntervalAndTime(List.of(RoleName.USER), IS_WEEK, today);
        long monthlyNumberOfCustomer = countUserByIntervalAndTime(List.of(RoleName.USER), IS_MONTH, today);
        long yearlyNumberOfCustomer = countUserByIntervalAndTime(List.of(RoleName.USER), IS_YEAR, today);

        long yesterdayNumberOfCustomer =
                countUserByIntervalAndTime(List.of(RoleName.USER), IS_DAY, today.minusSeconds(86400));
        long lastWeekNumberOfCustomer =
                countUserByIntervalAndTime(List.of(RoleName.USER), IS_WEEK, today.minusSeconds(7L * 86400));
        long lastMonthNumberOfCustomer =
                countUserByIntervalAndTime(List.of(RoleName.USER), IS_MONTH, today.minusSeconds(30L * 86400));
        long lastYearNumberOfCustomer =
                countUserByIntervalAndTime(List.of(RoleName.USER), IS_YEAR, today.minusSeconds(365L * 86400));

        long numberOfCICYesterday = calculateCustomerIncrease(dailyNumberOfCustomer, yesterdayNumberOfCustomer);
        long numberOfCICLastWeek = calculateCustomerIncrease(weeklyNumberOfCustomer, lastWeekNumberOfCustomer);
        long numberOfCICLastMonth = calculateCustomerIncrease(monthlyNumberOfCustomer, lastMonthNumberOfCustomer);
        long numberOfCICLastYear = calculateCustomerIncrease(yearlyNumberOfCustomer, lastYearNumberOfCustomer);

        int dailyCustomerGrowthRate = calculateGrowthRateByRoleName(dailyNumberOfCustomer, yesterdayNumberOfCustomer);
        int weeklyCustomerGrowthRate = calculateGrowthRateByRoleName(weeklyNumberOfCustomer, lastWeekNumberOfCustomer);
        int monthlyCustomerGrowthRate =
                calculateGrowthRateByRoleName(monthlyNumberOfCustomer, lastMonthNumberOfCustomer);
        int yearlyCustomerGrowthRate = calculateGrowthRateByRoleName(yearlyNumberOfCustomer, lastYearNumberOfCustomer);

        long dailyNumberOfSeller = countStoreByIntervalAndTime(List.of(RoleName.SELLER), IS_DAY, today);
        long weeklyNumberOfSeller = countStoreByIntervalAndTime(List.of(RoleName.SELLER), IS_WEEK, today);
        long monthlyNumberOfSeller = countStoreByIntervalAndTime(List.of(RoleName.SELLER), IS_MONTH, today);
        long yearlyNumberOfSeller = countStoreByIntervalAndTime(List.of(RoleName.SELLER), IS_YEAR, today);

        long yesterdayNumberOfSeller =
                countStoreByIntervalAndTime(List.of(RoleName.SELLER), IS_DAY, today.minusSeconds(86400));
        long lastWeekNumberOfSeller =
                countStoreByIntervalAndTime(List.of(RoleName.SELLER), IS_WEEK, today.minusSeconds(7L * 86400));
        long lastMonthNumberOfSeller =
                countStoreByIntervalAndTime(List.of(RoleName.SELLER), IS_MONTH, today.minusSeconds(30L * 86400));
        long lastYearNumberOfSeller =
                countStoreByIntervalAndTime(List.of(RoleName.SELLER), IS_YEAR, today.minusSeconds(365L * 86400));

        long numberOfSICYesterday = calculateSellerIncrease(dailyNumberOfSeller, yesterdayNumberOfSeller);
        long numberOfSICLastWeek = calculateSellerIncrease(weeklyNumberOfSeller, lastWeekNumberOfSeller);
        long numberOfSICLastMonth = calculateSellerIncrease(monthlyNumberOfSeller, lastMonthNumberOfSeller);
        long numberOfSICLastYear = calculateSellerIncrease(yearlyNumberOfSeller, lastYearNumberOfSeller);

        int dailySellerGrowthRate = calculateGrowthRateByRoleName(dailyNumberOfSeller, yesterdayNumberOfSeller);
        int weeklySellerGrowthRate = calculateGrowthRateByRoleName(weeklyNumberOfSeller, lastWeekNumberOfSeller);
        int monthlySellerGrowthRate = calculateGrowthRateByRoleName(monthlyNumberOfSeller, lastMonthNumberOfSeller);
        int yearlySellerGrowthRate = calculateGrowthRateByRoleName(yearlyNumberOfSeller, lastYearNumberOfSeller);

        List<Object[]> rawTop5Stores = oshRepository.findTop5StoresByRevenueRaw();
        List<StoreRevenueResponse> top5StoresByRevenue = rawTop5Stores.stream()
                .map(row -> new StoreRevenueResponse((String) row[0], (String) row[1], (BigDecimal) row[2]))
                .toList();

        return AdminStatisticsResponse.builder()
                .numberOfOrdersWaitingForShipping(numberOfOrdersWaitingForShipping)
                .numberOfOrdersPickedUp(numberOfOrdersPickedUp)
                .numberOfOrdersOutForDelivery(numberOfOrdersOutForDelivery)
                .numberOfOrdersCancelled(numberOfOrdersCancelled)
                .numberOfOrdersDelivered(numberOfOrdersDelivered)
                .numberOfOrdersPending(numberOfOrdersPending)
                .dailyRevenue(dailyRevenue)
                .weeklyRevenue(weeklyRevenue)
                .monthlyRevenue(monthlyRevenue)
                .yearlyRevenue(yearlyRevenue)
                .revenueIncreaseCompareYesterday(revenueIncreaseCompareYesterday)
                .revenueIncreaseCompareLastWeek(revenueIncreaseCompareLastWeek)
                .revenueIncreaseCompareLastMonth(revenueIncreaseCompareLastMonth)
                .revenueIncreaseCompareLastYear(revenueIncreaseCompareLastYear)
                .dailyRevenueGrowthRate(dailyRevenueGrowthRate)
                .weeklyRevenueGrowthRate(weeklyRevenueGrowthRate)
                .monthlyRevenueGrowthRate(monthlyRevenueGrowthRate)
                .yearlyRevenueGrowthRate(yearlyRevenueGrowthRate)
                .totalNumberOfCustomers(totalNumberOfCustomers)
                .totalNumberOfSellers(totalNumberOfSellers)
                .totalNumberOfAdmins(totalNumberOfAdmins)
                .dailyNumberOfCustomer(dailyNumberOfCustomer)
                .weeklyNumberOfCustomer(weeklyNumberOfCustomer)
                .monthlyNumberOfCustomer(monthlyNumberOfCustomer)
                .yearlyNumberOfCustomer(yearlyNumberOfCustomer)
                .numberOfCustomersIncreaseCompareYesterday(numberOfCICYesterday)
                .numberOfCustomersIncreaseCompareLastWeek(numberOfCICLastWeek)
                .numberOfCustomersIncreaseCompareLastMonth(numberOfCICLastMonth)
                .numberOfCustomersIncreaseCompareLastYear(numberOfCICLastYear)
                .dailyCustomerGrowthRate(dailyCustomerGrowthRate)
                .weeklyCustomerGrowthRate(weeklyCustomerGrowthRate)
                .monthlyCustomerGrowthRate(monthlyCustomerGrowthRate)
                .yearlyCustomerGrowthRate(yearlyCustomerGrowthRate)
                .dailyNumberOfSeller(dailyNumberOfSeller)
                .weeklyNumberOfSeller(weeklyNumberOfSeller)
                .monthlyNumberOfSeller(monthlyNumberOfSeller)
                .yearlyNumberOfSeller(yearlyNumberOfSeller)
                .numberOfSellersIncreaseCompareYesterday(numberOfSICYesterday)
                .numberOfSellersIncreaseCompareLastWeek(numberOfSICLastWeek)
                .numberOfSellersIncreaseCompareLastMonth(numberOfSICLastMonth)
                .numberOfSellersIncreaseCompareLastYear(numberOfSICLastYear)
                .dailySellerGrowthRate(dailySellerGrowthRate)
                .weeklySellerGrowthRate(weeklySellerGrowthRate)
                .monthlySellerGrowthRate(monthlySellerGrowthRate)
                .yearlySellerGrowthRate(yearlySellerGrowthRate)
                .top5StoresByRevenue(top5StoresByRevenue)
                .build();
    }

    long getNumberOfOrdersByStatus(String status) {
        return oshRepository.countByLatestStatus(status);
    }

    BigDecimal calculateRevenue(String interval, Instant time) {
        return oshRepository.calculateRevenueByIntervalAndTime(interval, time);
    }

    BigDecimal calculateRevenueIncrease(BigDecimal currentRevenue, BigDecimal previousRevenue) {
        return currentRevenue.subtract(previousRevenue);
    }

    long calculateCustomerIncrease(long current, long previous) {
        return current - previous;
    }

    long calculateSellerIncrease(long current, long previous) {
        return current - previous;
    }

    long countUserByIntervalAndTime(List<RoleName> roles, String interval, Instant time) {
        return userRepository.countUserByIntervalAndTime(roles, interval, time);
    }

    long countStoreByIntervalAndTime(List<RoleName> roles, String interval, Instant time) {
        return storeRepository.countStoreByIntervalAndTime(roles, interval, time);
    }

    int calculateRevenueGrowthRate(BigDecimal currentRevenue, BigDecimal previousRevenue) {
        if (Objects.nonNull(previousRevenue) && previousRevenue.compareTo(BigDecimal.ZERO) > 0) {
            return currentRevenue
                    .subtract(previousRevenue)
                    .divide(previousRevenue, 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .intValue();
        }
        return 0;
    }

    int calculateGrowthRateByRoleName(long current, long previous) {
        if (previous > 0) {
            return (int) (((double) (current - previous) / previous) * 100);
        }
        return 0;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public RevenueOneYearResponse getRevenueOneYear(String year, String month) {
        int yearInt;
        int monthInt;

        try {
            yearInt = Integer.parseInt(year);
            if (yearInt < 2020) {
                throw new AppException(ErrorCode.INVALID_YEAR);
            }
        } catch (NumberFormatException e) {
            throw new AppException(ErrorCode.INVALID_YEAR);
        }

        try {
            monthInt = Integer.parseInt(month);
            if (monthInt < 1 || monthInt > 12) {
                throw new AppException(ErrorCode.INVALID_MONTH);
            }
        } catch (NumberFormatException e) {
            throw new AppException(ErrorCode.INVALID_MONTH);
        }

        LocalDate firstDayOfMonth = LocalDate.of(yearInt, monthInt, 1);
        LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());

        Instant startDate = firstDayOfMonth.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endDate = lastDayOfMonth.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        List<RevenueOneDayResponse> revenueOneDayResponses = new ArrayList<>();
        Instant currentDate = startDate;

        while (currentDate.isBefore(endDate)) {
            Instant nextDate = currentDate.plusSeconds(86400);

            BigDecimal dailyRevenue = oshRepository.calculateDailyRevenue("DELIVERED", currentDate, nextDate);

            String formattedDate =
                    LocalDate.ofInstant(currentDate, ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE);

            revenueOneDayResponses.add(RevenueOneDayResponse.builder()
                    .revenue(dailyRevenue != null ? dailyRevenue : BigDecimal.ZERO)
                    .date(formattedDate)
                    .build());

            currentDate = nextDate;
        }

        BigDecimal totalRevenueOneYear = oshRepository.calculateTotalRevenueOneYear(yearInt);

        return RevenueOneYearResponse.builder()
                .revenueOneDayResponses(revenueOneDayResponses)
                .totalRevenueOneYear(totalRevenueOneYear)
                .build();
    }
}
