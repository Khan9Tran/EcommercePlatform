package com.hkteam.ecommerce_platform.entity.user;

import java.time.Instant;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.persistence.CascadeType;

import org.hibernate.annotations.*;

import com.hkteam.ecommerce_platform.entity.cart.Cart;
import com.hkteam.ecommerce_platform.entity.order.Order;
import com.hkteam.ecommerce_platform.entity.product.Product;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SQLDelete(sql = "UPDATE store SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(nullable = false, unique = true)
    String slug;

    @Column(nullable = false)
    String name;

    String bio;

    Float rating;

    @Column(name = "default_address_id")
    Long defaultAddressId;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Product> products;

    @OneToOne
    User user;

    @OneToMany(mappedBy = "store")
    Set<Order> orders;

    @OneToMany(mappedBy = "store")
    Set<Cart> carts;

    @CreationTimestamp(source = SourceType.DB)
    Instant createdAt;

    @UpdateTimestamp(source = SourceType.DB)
    Instant lastUpdatedAt;

    @Column(nullable = false)
    boolean isDeleted = Boolean.FALSE;

    @Column(nullable = false)
    boolean isBanned = Boolean.FALSE;
}
