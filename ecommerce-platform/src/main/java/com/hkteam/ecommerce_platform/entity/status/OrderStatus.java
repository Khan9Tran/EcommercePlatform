package com.hkteam.ecommerce_platform.entity.status;

import com.hkteam.ecommerce_platform.entity.order.OrderStatusHistory;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import java.util.Set;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SQLDelete(sql = "UPDATE status SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderStatus extends Status {
    @OneToMany(mappedBy = "orderStatus")
    Set<OrderStatusHistory> orderStatusHistories;
}
