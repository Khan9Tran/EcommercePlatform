package com.hkteam.ecommerce_platform.entity.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import com.hkteam.ecommerce_platform.entity.order.Order;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SQLDelete(sql = "UPDATE transactions SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @OneToOne(cascade = CascadeType.ALL)
    Order order;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<TransactionStatusHistory> transactionStatusHistories;

    @ManyToOne(cascade = CascadeType.ALL)
    Payment payment;

    BigDecimal amount;


    @CreationTimestamp
    Instant createdAt;

    @UpdateTimestamp
    Instant lastUpdatedAt;

    @Column(nullable = false)
    boolean isDeleted = Boolean.FALSE;
}
