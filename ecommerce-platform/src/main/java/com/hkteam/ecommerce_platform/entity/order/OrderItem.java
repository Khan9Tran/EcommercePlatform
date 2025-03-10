package com.hkteam.ecommerce_platform.entity.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.validation.constraints.Min;

import org.hibernate.annotations.*;

import com.hkteam.ecommerce_platform.entity.product.Product;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SQLDelete(sql = "UPDATE item SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    Product product;

    List<String> values;

    @ManyToOne(cascade = CascadeType.ALL)
    Order order;

    @Min(1)
    int quantity;

    BigDecimal price;

    BigDecimal discount;

    @CreationTimestamp(source = SourceType.DB)
    Instant createdAt;

    @UpdateTimestamp(source = SourceType.DB)
    Instant lastUpdatedAt;

    @Column(nullable = false)
    boolean isDeleted = Boolean.FALSE;
}
