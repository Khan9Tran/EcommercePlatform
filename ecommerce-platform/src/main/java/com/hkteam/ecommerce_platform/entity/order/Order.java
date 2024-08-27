package com.hkteam.ecommerce_platform.entity.order;

import com.hkteam.ecommerce_platform.entity.User.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SQLDelete(sql = "UPDATE order SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order {
    @Id
    @GeneratedValue (strategy = GenerationType.UUID)
    String id;

    @ManyToOne
    User user;

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

    @ManyToOne
    Status status;

    @OneToMany
    Set<OrderItem> items;
}
