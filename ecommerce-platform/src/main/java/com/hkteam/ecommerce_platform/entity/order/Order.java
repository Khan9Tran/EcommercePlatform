package com.hkteam.ecommerce_platform.entity.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;

import org.hibernate.annotations.*;

import com.hkteam.ecommerce_platform.entity.payment.Transaction;
import com.hkteam.ecommerce_platform.entity.user.Store;
import com.hkteam.ecommerce_platform.entity.user.User;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SQLDelete(sql = "UPDATE order SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
@Table(name = "orders")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne
    User user;

    @ManyToOne
    Store store;

    BigDecimal total;
    BigDecimal discount;

    String phone;
    String recipientName;

    String province;
    String district;
    String subDistrict;
    String detailAddress;
    String detailLocate;

    BigDecimal shippingFee;
    BigDecimal shippingDiscount;
    BigDecimal shippingTotal;

    BigDecimal grandTotal; // total - discount + shippingTotal
    BigDecimal promo; // discount + shippingDiscount
    String note;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    List<OrderStatusHistory> orderStatusHistories;

    @OneToMany(mappedBy = "order")
    List<OrderItem> orderItems;

    @OneToOne(mappedBy = "order")
    Transaction transaction;

    @CreationTimestamp(source = SourceType.DB)
    private Instant createdAt;

    @UpdateTimestamp(source = SourceType.DB)
    private Instant lastUpdatedAt;

    @Column(nullable = false)
    boolean isDeleted = Boolean.FALSE;
}
