package com.hkteam.ecommerce_platform.entity.order;

import java.time.Instant;

import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import com.hkteam.ecommerce_platform.entity.status.OrderStatus;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SQLDelete(sql = "UPDATE order_status_history SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(cascade = CascadeType.ALL)
    Order order;

    @ManyToOne
    OrderStatus orderStatus;

    String remarks;

    @CreationTimestamp
    Instant createdAt;

    @UpdateTimestamp
    Instant lastUpdatedAt;

    @Column(nullable = false)
    boolean isDeleted = Boolean.FALSE;
}
