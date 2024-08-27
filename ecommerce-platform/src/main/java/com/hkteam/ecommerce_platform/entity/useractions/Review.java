package com.hkteam.ecommerce_platform.entity.useractions;

import com.hkteam.ecommerce_platform.entity.Product.Product;
import com.hkteam.ecommerce_platform.entity.User.User;
import com.hkteam.ecommerce_platform.entity.image.ReviewImage;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.*;

import java.time.Instant;
import java.util.Set;

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

    @OneToOne
    User user;

    @OneToOne
    Product product;

    float rating;

    String comment;

    String videoUrl;

    @OneToMany(cascade = CascadeType.ALL)
    Set<ReviewImage> images;

    @CreationTimestamp(source = SourceType.DB)
    private Instant createdAt;

    @UpdateTimestamp(source = SourceType.DB)
    private Instant lastUpdatedAt;

    @Column(nullable = false)
    boolean isDeleted = Boolean.FALSE;
}
