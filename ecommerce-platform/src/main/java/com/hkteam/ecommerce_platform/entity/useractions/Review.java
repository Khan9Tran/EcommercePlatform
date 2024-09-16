package com.hkteam.ecommerce_platform.entity.useractions;

import java.time.Instant;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.persistence.CascadeType;

import org.hibernate.annotations.*;

import com.hkteam.ecommerce_platform.entity.image.ReviewImage;
import com.hkteam.ecommerce_platform.entity.product.Product;
import com.hkteam.ecommerce_platform.entity.user.User;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SQLDelete(sql = "UPDATE review SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    User user;

    @ManyToOne
    Product product;

    float rating;

    String comment;

    String videoUrl;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL)
    Set<ReviewImage> images;

    @CreationTimestamp(source = SourceType.DB)
    private Instant createdAt;

    @UpdateTimestamp(source = SourceType.DB)
    private Instant lastUpdatedAt;

    @Column(nullable = false)
    boolean isDeleted = Boolean.FALSE;
}
