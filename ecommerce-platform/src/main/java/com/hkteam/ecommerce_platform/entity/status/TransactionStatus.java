package com.hkteam.ecommerce_platform.entity.status;

import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.hkteam.ecommerce_platform.entity.payment.TransactionStatusHistory;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SQLDelete(sql = "UPDATE status SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionStatus extends Status {
    @OneToMany(mappedBy = "transactionStatus")
    Set<TransactionStatusHistory> transactionStatusHistories;
}
