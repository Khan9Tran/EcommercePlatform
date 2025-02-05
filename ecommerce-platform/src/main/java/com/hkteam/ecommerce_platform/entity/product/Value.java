package com.hkteam.ecommerce_platform.entity.product;

import java.time.Instant;
import java.util.Set;

import jakarta.persistence.*;

import org.hibernate.annotations.*;

import com.hkteam.ecommerce_platform.entity.user.User;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SQLDelete(sql = "UPDATE value SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Value {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String value;

    @ManyToOne
    User createdBy;

    @ManyToOne
    Attribute attribute;

    @ManyToMany(mappedBy = "values")
    Set<Variant> variants;

    @CreationTimestamp(source = SourceType.DB)
    Instant createdAt;

    @UpdateTimestamp(source = SourceType.DB)
    Instant lastUpdatedAt;

    @Column(nullable = false)
    boolean isDeleted = Boolean.FALSE;
}
