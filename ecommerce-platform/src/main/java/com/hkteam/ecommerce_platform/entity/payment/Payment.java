package com.hkteam.ecommerce_platform.entity.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import com.hkteam.ecommerce_platform.enums.PaymentMethod;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE payment SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @OneToMany(mappedBy = "payment")
    Set<Transaction> transactions;

    BigDecimal amount;
    PaymentMethod paymentMethod;
    String paymentDetails;

    @CreationTimestamp
    Instant createdAt;

    @UpdateTimestamp
    Instant lastUpdatedAt;

    @Column(nullable = false)
    boolean isDeleted = Boolean.FALSE;
}
