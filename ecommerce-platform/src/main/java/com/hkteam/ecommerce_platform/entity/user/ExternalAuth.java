package com.hkteam.ecommerce_platform.entity.user;

import java.time.Instant;

import jakarta.persistence.*;

import jakarta.persistence.CascadeType;
import org.hibernate.annotations.*;

import com.hkteam.ecommerce_platform.enums.Provider;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SQLDelete(sql = "UPDATE external_auth SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExternalAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;


    @ManyToOne(cascade = CascadeType.PERSIST)
    User user;

    String providerID;

    Provider provider;

    @CreationTimestamp(source = SourceType.DB)
    Instant createdAt;

    @UpdateTimestamp(source = SourceType.DB)
    Instant lastUpdatedAt;

    @Column(nullable = false)
    boolean isDeleted = Boolean.FALSE;
}
