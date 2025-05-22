package com.hkteam.ecommerce_platform.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatisticRequest {

    public enum GroupByType {
        DAY("YYYY-MM-DD"),
        WEEK("IYYY-IW"),
        MONTH("YYYY-MM");

        private final String postgresFormat;

        GroupByType(String postgresFormat) {
            this.postgresFormat = postgresFormat;
        }

        public String getPostgresFormat() {
            return postgresFormat;
        }
    }

    public enum DateRangeType {
        CUSTOM,
        TODAY,
        YESTERDAY,
        LAST_7_DAYS,
        LAST_30_DAYS,
        THIS_WEEK,
        LAST_WEEK,
        THIS_MONTH,
        LAST_MONTH
    }

    /**
     * Thời gian bắt đầu - chỉ dùng khi rangeType là CUSTOM
     */
    LocalDate from;

    /**
     * Thời gian kết thúc - chỉ dùng khi rangeType là CUSTOM
     */
    LocalDate to;

    /**
     * Loại khoảng thời gian (hỗ trợ chọn nhanh)
     * Ưu tiên xử lý trước nếu có
     */
    DateRangeType rangeType; //custom thì truyền from và to còn trường hợp khác chỉ cần truyền rangeType

    /**
     * Nhóm thống kê theo ngày / tuần / tháng
     */
    GroupByType groupBy; //phải truyền vào

    /**
     * ID của store nếu filterBy là BY_STORE
     */
    String storeId;

    /**
     * ID của product nếu filterBy là BY_PRODUCT
     */
    String productId;

    /**
     * Số lượng top item trả về (nếu cần lấy top sản phẩm, top cửa hàng,...)
     */

    String type;  //"REVENUE" "PRODUCT_SOLD" "ORDER_COUNT"
    Integer limit;
    Integer offset;
}
