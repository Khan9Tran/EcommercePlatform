package com.hkteam.ecommerce_platform.entity.product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

import org.hibernate.annotations.*;

import com.hkteam.ecommerce_platform.entity.cart.CartItem;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SQLDelete(sql = "UPDATE product SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Variant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(nullable = false, unique = true)
    String slug;

    BigDecimal originalPrice;
    BigDecimal salePrice;

    boolean isAvailable = Boolean.TRUE;

    @Min(0)
    int quantity;

    @Min(0)
    int sold;

    @OneToMany(mappedBy = "variant")
    Set<CartItem> cartItems;

    @ManyToMany
    List<Value> values;

    String imageUrl;

    @ManyToOne
    Product product;

    @CreationTimestamp(source = SourceType.DB)
    private Instant createdAt;

    @UpdateTimestamp(source = SourceType.DB)
    private Instant lastUpdatedAt;

    @Column(nullable = false)
    boolean isDeleted = Boolean.FALSE;

    @Version
    private int version;
}
