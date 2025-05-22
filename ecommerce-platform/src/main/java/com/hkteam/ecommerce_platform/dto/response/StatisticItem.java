package com.hkteam.ecommerce_platform.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatisticItem {

    public StatisticItem(Object dateFormatted, String str1, String entityName, String str3, String str4, int zero, String unit , BigDecimal total) {
        this.groupKey = dateFormatted.toString();
        this.amount = total;
        this.unit = unit;
        this.entityName = entityName;
    }


    /**
     * Khoá nhóm theo thời gian (ngày, tuần, tháng)
     */
    String groupKey;

    /**
     * ID của entity (productId, storeId,...)
     */
    String entityId;

    /**
     * Tên entity (tên sản phẩm, tên cửa hàng, ...)
     */
    String entityName;

    /**
     * Slug (đường dẫn thân thiện cho entity, ví dụ product slug)
     */
    String slug;

    /**
     * URL ảnh đại diện của entity
     */
    String imageUrl;

    /**
     * Tổng số lượng (ví dụ tổng số sản phẩm bán được, số đơn hàng,...)
     */
    Integer quantity;

    /**
     * Tổng doanh thu (hoặc tổng giá trị liên quan)
     */

    String unit;
    BigDecimal amount;
}