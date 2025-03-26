package com.hkteam.ecommerce_platform.entity.chat;

import com.hkteam.ecommerce_platform.entity.order.Order;
import com.hkteam.ecommerce_platform.entity.product.Product;
import com.hkteam.ecommerce_platform.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SQLDelete(sql = "UPDATE store SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String content;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    Room room;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User sender;


    @CreationTimestamp(source = SourceType.DB)
    Instant createdAt;

    @UpdateTimestamp(source = SourceType.DB)
    Instant lastUpdatedAt;

    @Column(nullable = false)
    boolean isDeleted = Boolean.FALSE;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = true) // Optional: Không phải tin nhắn nào cũng có sản phẩm
    Product product;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = true)
    Order order;


}
