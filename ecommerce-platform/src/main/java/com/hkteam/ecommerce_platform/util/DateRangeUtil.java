package com.hkteam.ecommerce_platform.util;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import com.hkteam.ecommerce_platform.dto.request.StatisticRequest.DateRangeType;

public class DateRangeUtil {

    /**
     * Trả về from và to dựa vào rangeType hoặc from/to truyền vào nếu là CUSTOM.
     */
    public static LocalDate[] resolveFromTo(DateRangeType rangeType, LocalDate customFrom, LocalDate customTo) {
        LocalDate today = LocalDate.now();
        LocalDate from;
        LocalDate to;

        if (rangeType == null || rangeType == DateRangeType.CUSTOM) {
            // Nếu CUSTOM hoặc không có rangeType thì sử dụng from/to truyền vào
            from = customFrom;
            to = customTo;
        } else {
            switch (rangeType) {
                case TODAY:
                    from = to = today;
                    break;
                case YESTERDAY:
                    from = to = today.minusDays(1);
                    break;
                case LAST_7_DAYS:
                    from = today.minusDays(6); // bao gồm cả hôm nay
                    to = today;
                    break;
                case LAST_30_DAYS:
                    from = today.minusDays(29);
                    to = today;
                    break;
                case THIS_WEEK:
                    from = today.with(DayOfWeek.MONDAY);
                    to = today.with(DayOfWeek.SUNDAY);
                    break;
                case LAST_WEEK:
                    from = today.minusWeeks(1).with(DayOfWeek.MONDAY);
                    to = today.minusWeeks(1).with(DayOfWeek.SUNDAY);
                    break;
                case THIS_MONTH:
                    from = today.withDayOfMonth(1);
                    to = today.with(TemporalAdjusters.lastDayOfMonth());
                    break;
                case LAST_MONTH:
                    LocalDate firstDayLastMonth = today.minusMonths(1).withDayOfMonth(1);
                    from = firstDayLastMonth;
                    to = firstDayLastMonth.with(TemporalAdjusters.lastDayOfMonth());
                    break;
                default:
                    from = customFrom;
                    to = customTo;
            }
        }

        return new LocalDate[]{from, to};
    }

    public static Instant[] convertLocalDateRangeToInstant(LocalDate[] localDates) {
        ZoneId zoneId = ZoneId.systemDefault();

        Instant fromInstant = localDates[0].atStartOfDay(zoneId).toInstant();
        Instant toInstant = localDates[1].plusDays(1).atStartOfDay(zoneId).toInstant().minusSeconds(1); // lấy đến cuối ngày

        return new Instant[]{fromInstant, toInstant};
    }


}
