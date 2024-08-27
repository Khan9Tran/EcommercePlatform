package com.hkteam.ecommerce_platform.entity.product;

import com.hkteam.ecommerce_platform.entity.image.VariantImage;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

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

    int quantity;

    @OneToMany
    Set<Value> values;

    String imageUrl;

    @ManyToOne
    Product product;

    @OneToMany(cascade = CascadeType.ALL)
    Set<VariantImage> images;

    @CreationTimestamp(source = SourceType.DB)
    private Instant createdAt;

    @UpdateTimestamp(source = SourceType.DB)
    private Instant lastUpdatedAt;

    @Column(nullable = false)
    boolean isDeleted = Boolean.FALSE;


}
