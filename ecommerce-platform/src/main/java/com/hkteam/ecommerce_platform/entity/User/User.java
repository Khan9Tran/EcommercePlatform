package com.hkteam.ecommerce_platform.entity.User;

import com.hkteam.ecommerce_platform.entity.Product.Product;
import com.hkteam.ecommerce_platform.entity.authorization.Role;
import com.hkteam.ecommerce_platform.enums.Gender;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

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

    @Column(unique = true)
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


    @OneToMany
    Set<Address> addresses;

    @OneToOne
    Address defaultAddress;

    @ManyToMany
    @JoinTable(
            name = "user_store_following",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "store_id")
    )
    Set<Store> followingStores;

    @ManyToMany
    @JoinTable(
            name = "user_product_following",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    Set<Product> followingProducts;

    @CreationTimestamp(source = SourceType.DB)
    private Instant createdAt;

    @UpdateTimestamp(source = SourceType.DB)
    private Instant lastUpdatedAt;

    @Column(nullable = false)
    boolean isDeleted = Boolean.FALSE;

}
