package com.hkteam.ecommerce_platform.entity.category;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;

import org.hibernate.annotations.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SQLDelete(sql = "UPDATE component SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Component {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    boolean required = false;

    @ManyToMany(mappedBy = "components")
    List<Category> categories;

    @OneToMany(mappedBy = "component")
    Set<ProductComponentValue> productComponentValues;

    @CreationTimestamp(source = SourceType.DB)
    Instant createdAt;

    @UpdateTimestamp(source = SourceType.DB)
    Instant lastUpdatedAt;

    @Column(nullable = false)
    boolean isDeleted = Boolean.FALSE;
}
