package com.hkteam.ecommerce_platform.entity.authorization;

import java.time.Instant;
import java.util.Set;

import jakarta.persistence.*;

import org.hibernate.annotations.*;

import com.hkteam.ecommerce_platform.enums.RoleName;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLDelete(sql = "UPDATE role SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
@Entity
public class Role {
    @Id
    @Enumerated(EnumType.STRING)
    RoleName name;

    String description;

    @ManyToMany
    Set<Permission> permissions;

    @CreationTimestamp(source = SourceType.DB)
    private Instant createdAt;

    @UpdateTimestamp(source = SourceType.DB)
    private Instant lastUpdatedAt;

    @Column(nullable = false)
    boolean isDeleted = Boolean.FALSE;
}
