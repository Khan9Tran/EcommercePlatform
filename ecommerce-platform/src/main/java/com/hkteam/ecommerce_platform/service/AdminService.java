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

    @PreAuthorize("hasRole('ADMIN')")
    public AdminStatisticsResponse getAdminStatistic() {
        long numberOfOrdersWaitingForShipping = oshRepository.countByLatestStatus("WAITING_FOR_SHIPPING");
        long numberOfOrdersPickedUp = oshRepository.countByLatestStatus("PICKED_UP");
        long numberOfOrdersOutForDelivery = oshRepository.countByLatestStatus("OUT_FOR_DELIVERY");
        long numberOfOrdersCancelled = oshRepository.countByLatestStatus("CANCELLED");
        long numberOfOrdersDelivered = oshRepository.countByLatestStatus("DELIVERED");
        long numberOfOrdersPending = oshRepository.countByLatestStatus("PENDING");

        Instant today = Instant.now();

        BigDecimal dailyRevenue = oshRepository.calculateRevenueByIntervalAndTime("isDay", today);
        BigDecimal weeklyRevenue = oshRepository.calculateRevenueByIntervalAndTime("isWeek", today);
        BigDecimal monthlyRevenue = oshRepository.calculateRevenueByIntervalAndTime("isMonth", today);
        BigDecimal yearlyRevenue = oshRepository.calculateRevenueByIntervalAndTime("isYear", today);

        BigDecimal yesterdayRevenue =
                oshRepository.calculateRevenueByIntervalAndTime("isDay", today.minusSeconds(86400));
        BigDecimal lastWeekRevenue =
                oshRepository.calculateRevenueByIntervalAndTime("isWeek", today.minusSeconds(7L * 86400));
        BigDecimal lastMonthRevenue =
                oshRepository.calculateRevenueByIntervalAndTime("isMonth", today.minusSeconds(30L * 86400));
        BigDecimal lastYearRevenue =
                oshRepository.calculateRevenueByIntervalAndTime("isYear", today.minusSeconds(365L * 86400));

        BigDecimal revenueIncreaseCompareYesterday = dailyRevenue.subtract(yesterdayRevenue);
        BigDecimal revenueIncreaseCompareLastWeek = weeklyRevenue.subtract(lastWeekRevenue);
        BigDecimal revenueIncreaseCompareLastMonth = monthlyRevenue.subtract(lastMonthRevenue);
        BigDecimal revenueIncreaseCompareLastYear = yearlyRevenue.subtract(lastYearRevenue);

        int dailyRevenueGrowthRate = 0;
        if (Objects.nonNull(yesterdayRevenue) && yesterdayRevenue.compareTo(BigDecimal.ZERO) > 0) {
            dailyRevenueGrowthRate = dailyRevenue
                    .subtract(yesterdayRevenue)
                    .divide(yesterdayRevenue, 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .intValue();
        }

        int weeklyRevenueGrowthRate = 0;
        if (Objects.nonNull(lastMonthRevenue) && lastWeekRevenue.compareTo(BigDecimal.ZERO) > 0) {
            weeklyRevenueGrowthRate = weeklyRevenue
                    .subtract(lastWeekRevenue)
                    .divide(lastWeekRevenue, 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .intValue();
        }

        int monthlyRevenueGrowthRate = 0;
        if (Objects.nonNull(lastMonthRevenue) && lastMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
            monthlyRevenueGrowthRate = monthlyRevenue
                    .subtract(lastMonthRevenue)
                    .divide(lastMonthRevenue, 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .intValue();
        }

        int yearlyRevenueGrowthRate = 0;
        if (Objects.nonNull(lastYearRevenue) && lastYearRevenue.compareTo(BigDecimal.ZERO) > 0) {
            yearlyRevenueGrowthRate = yearlyRevenue
                    .subtract(lastYearRevenue)
                    .divide(lastYearRevenue, 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .intValue();
        }

        long totalNumberOfCustomers = userRepository.countByRolesName(RoleName.USER);
        long totalNumberOfSellers = userRepository.countByRolesName(RoleName.SELLER);
        long totalNumberOfAdmins = userRepository.countByRolesName(RoleName.ADMIN);

        long customersYesterday =
                userRepository.countCustomersCreatedSince(List.of(RoleName.USER), today.minusSeconds(86400));
        long customersLastWeek =
                userRepository.countCustomersCreatedSince(List.of(RoleName.USER), today.minusSeconds(7L * 86400));
        long customersLastMonth =
                userRepository.countCustomersCreatedSince(List.of(RoleName.USER), today.minusSeconds(30L * 86400));
        long customersLastYear =
                userRepository.countCustomersCreatedSince(List.of(RoleName.USER), today.minusSeconds(365L * 86400));

        long numberOfCustomersIncreaseCompareYesterday = totalNumberOfCustomers - customersYesterday;
        long numberOfCustomersIncreaseCompareLastWeek = totalNumberOfCustomers - customersLastWeek;
        long numberOfCustomersIncreaseCompareLastMonth = totalNumberOfCustomers - customersLastMonth;
        long numberOfCustomersIncreaseCompareLastYear = totalNumberOfCustomers - customersLastYear;

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
                .numberOfCustomersIncreaseCompareYesterday(numberOfCustomersIncreaseCompareYesterday)
                .numberOfCustomersIncreaseCompareLastWeek(numberOfCustomersIncreaseCompareLastWeek)
                .numberOfCustomersIncreaseCompareLastMonth(numberOfCustomersIncreaseCompareLastMonth)
                .numberOfCustomersIncreaseCompareLastYear(numberOfCustomersIncreaseCompareLastYear)
                .top5StoresByRevenue(top5StoresByRevenue)
                .build();
    }

    //    @PreAuthorize("hasRole('ADMIN')")
    //    public RevenueOneYearResponse getRevenueOneYear(String year) {
    //        int yearInt;
    //        try {
    //            yearInt = Integer.parseInt(year);
    //        } catch (NumberFormatException e) {
    //            yearInt = LocalDate.now().getYear();
    //        }
    //
    //        Instant startDate = LocalDate.of(yearInt, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC);
    //        Instant endDate = LocalDate.of(yearInt, 12, 31).atStartOfDay().toInstant(ZoneOffset.UTC);
    //
    //        List<RevenueOneDayResponse> revenueOneDayResponses = new ArrayList<>();
    //        Instant currentDate = startDate;
    //
    //        while (!currentDate.isAfter(endDate)) {
    //            Instant nextDate = currentDate.plusSeconds(86400);
    //
    //            BigDecimal dailyRevenue = oshRepository.calculateDailyRevenue("DELIVERED", currentDate, nextDate);
    //
    //            String formattedDate =
    //                    LocalDate.ofInstant(currentDate, ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE);
    //
    //            revenueOneDayResponses.add(RevenueOneDayResponse.builder()
    //                    .revenue(dailyRevenue != null ? dailyRevenue : BigDecimal.ZERO)
    //                    .date(formattedDate)
    //                    .build());
    //
    //            currentDate = nextDate;
    //        }
    //
    //        return RevenueOneYearResponse.builder()
    //                .revenueOneDayResponses(revenueOneDayResponses)
    //                .build();
    //    }

    @PreAuthorize("hasRole('ADMIN')")
    public RevenueOneYearResponse getRevenueOneYear(String year, String month) {
        int yearInt;
        int monthInt;

        try {
            yearInt = Integer.parseInt(year);
        } catch (NumberFormatException e) {
            yearInt = LocalDate.now().getYear();
        }

        try {
            monthInt = Integer.parseInt(month);
            if (monthInt < 1 || monthInt > 12) {
                throw new AppException(ErrorCode.UNKNOWN_ERROR);
            }
        } catch (NumberFormatException e) {
            monthInt = LocalDate.now().getMonthValue();
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

        return RevenueOneYearResponse.builder()
                .revenueOneDayResponses(revenueOneDayResponses)
                .build();
    }
}
