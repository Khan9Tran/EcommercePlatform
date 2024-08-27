package com.hkteam.ecommerce_platform.entity.User;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SQLDelete(sql = "UPDATE address SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String recipientName;

    @Column(nullable = false)
    String phone;

    @Column(nullable = false)
    String province;

    @Column(nullable = false)
    String district;

    @Column(nullable = false)
    String detailAddress;

    String detailLocate;


    @CreationTimestamp(source = SourceType.DB)
    private Instant createdAt;

    @UpdateTimestamp(source = SourceType.DB)
    private Instant lastUpdatedAt;

    @Column(nullable = false)
    boolean isDeleted = Boolean.FALSE;


}
