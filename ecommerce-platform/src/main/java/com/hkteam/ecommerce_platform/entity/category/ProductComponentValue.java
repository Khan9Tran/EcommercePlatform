package com.hkteam.ecommerce_platform.entity.category;

import java.time.Instant;

import jakarta.persistence.*;

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
@SQLDelete(sql = "UPDATE product_component_value SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductComponentValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String value;

    @ManyToOne
    Component component;

    @ManyToOne
    Product product;

    @CreationTimestamp(source = SourceType.DB)
    private Instant createdAt;

    @UpdateTimestamp(source = SourceType.DB)
    private Instant lastUpdatedAt;

    @Column(nullable = false)
    boolean isDeleted = Boolean.FALSE;
}
