package com.hkteam.ecommerce_platform.entity.Product;

import com.hkteam.ecommerce_platform.entity.User.Store;
import com.hkteam.ecommerce_platform.entity.image.ProductImage;
import com.hkteam.ecommerce_platform.entity.useractions.Review;
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
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(nullable = false, unique = true)
    String slug;

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    String description;

    String details;
    String videoUrl;
    String mainImageUrl;

    BigDecimal originalPrice;
    BigDecimal salePrice;

    boolean isAvailable = Boolean.TRUE;

    int quantity;

    Float rating;

    @OneToMany(cascade = CascadeType.ALL)
    Set<ProductImage> images;

    @ManyToOne
    Brand brand;

    @ManyToOne
    Category category;

    @ManyToOne
    Store store;

    @OneToMany
    Set<Variant> variants;

    @OneToMany
    Set<Review> reviews;

    @CreationTimestamp(source = SourceType.DB)
    private Instant createdAt;

    @UpdateTimestamp(source = SourceType.DB)
    private Instant lastUpdatedAt;

    @Column(nullable = false)
    boolean isDeleted = Boolean.FALSE;
}
