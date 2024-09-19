package com.hkteam.ecommerce_platform.entity.user;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;

import org.hibernate.annotations.*;

import com.hkteam.ecommerce_platform.entity.authorization.Role;
import com.hkteam.ecommerce_platform.entity.cart.Cart;
import com.hkteam.ecommerce_platform.entity.order.Order;
import com.hkteam.ecommerce_platform.entity.product.Product;
import com.hkteam.ecommerce_platform.entity.useractions.Review;
import com.hkteam.ecommerce_platform.enums.Gender;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SQLDelete(sql = "UPDATE 'users' SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(unique = true, columnDefinition = "VARCHAR(255) COLLATE utf8mb4_unicode_ci")
    String username;

    String name;
    String bio;
    String passwordDigest;

    @Column(unique = true)
    String phone;

    String phoneValidationToken;
    Instant phoneTokenGeneratedAt;
    String phoneValidationStatus;

    @Column(unique = true)
    String email;

    String emailValidationToken;
    Instant emailTokenGeneratedAt;
    String emailValidationStatus;

    @Enumerated(EnumType.STRING)
    Gender gender;

    LocalDate dateOfBirth;
    String imageUrl;

    @ManyToMany
    Set<Role> roles;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Address> addresses;

    @Column(name = "default_address_id")
    Long defaultAddressId;

    @OneToOne(mappedBy = "user")
    Store store;

    @OneToMany(mappedBy = "user")
    Set<Order> orders;

    @OneToMany(mappedBy = "user")
    Set<ExternalAuth> externalAuth;

    @OneToMany(mappedBy = "user")
    Set<Review> reviews;

    @OneToMany(mappedBy = "user")
    Set<Cart> carts;

    @ManyToMany
    @JoinTable(
            name = "user_store_following",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "store_id"))
    Set<Store> followingStores;

    @ManyToMany
    @JoinTable(
            name = "user_product_following",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"))
    Set<Product> followingProducts;

    @CreationTimestamp(source = SourceType.DB)
    private Instant createdAt;

    @UpdateTimestamp(source = SourceType.DB)
    private Instant lastUpdatedAt;

    @Column(nullable = false)
    boolean isDeleted = Boolean.FALSE;
}
