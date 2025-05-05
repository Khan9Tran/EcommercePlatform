package com.hkteam.ecommerce_platform.entity.product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import org.hibernate.annotations.*;

import com.hkteam.ecommerce_platform.entity.cart.CartItem;
import com.hkteam.ecommerce_platform.entity.category.Category;
import com.hkteam.ecommerce_platform.entity.category.ProductComponentValue;
import com.hkteam.ecommerce_platform.entity.chat.Message;
import com.hkteam.ecommerce_platform.entity.image.ProductImage;
import com.hkteam.ecommerce_platform.entity.order.OrderItem;
import com.hkteam.ecommerce_platform.entity.user.Store;
import com.hkteam.ecommerce_platform.entity.user.User;
import com.hkteam.ecommerce_platform.entity.useractions.Review;
import com.hkteam.ecommerce_platform.entity.useractions.ViewProduct;

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

    @Size(max = 2000, message = "Details must not exceed 2000 characters")
    @Column(length = 2000)
    String details;

    String videoUrl;
    String mainImageUrl;

    BigDecimal originalPrice;
    BigDecimal salePrice;

    boolean isAvailable = Boolean.TRUE;

    @Min(0)
    int quantity;

    @Min(0)
    int sold;

    Float rating;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    Set<ProductImage> images;

    @ManyToOne
    Brand brand;

    @ManyToOne
    Category category;

    @ManyToOne
    Store store;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    Set<ProductComponentValue> productComponentValues;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    List<Variant> variants;

    @ManyToMany(mappedBy = "products")
    List<Review> reviews;

    @OneToMany(mappedBy = "product")
    Set<OrderItem> orderItems;

    @OneToMany(mappedBy = "product")
    Set<CartItem> cartItems;

    @ManyToMany(mappedBy = "followingProducts")
    Set<User> followers;

    @OneToMany(mappedBy = "product")
    Set<ViewProduct> viewProducts;

    @CreationTimestamp(source = SourceType.DB)
    Instant createdAt;

    @UpdateTimestamp(source = SourceType.DB)
    Instant lastUpdatedAt;

    @Column(nullable = false)
    boolean isDeleted = Boolean.FALSE;

    @Column(nullable = false)
    boolean isBlocked = Boolean.FALSE;

    @Version
    int version;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Message> messages = new ArrayList<>();
}
