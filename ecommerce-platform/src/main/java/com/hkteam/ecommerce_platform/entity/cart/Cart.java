package com.hkteam.ecommerce_platform.entity.cart;

import java.util.Set;

import jakarta.persistence.*;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.hkteam.ecommerce_platform.entity.user.Store;
import com.hkteam.ecommerce_platform.entity.user.User;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLDelete(sql = "UPDATE cart SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
@Entity
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    Store store;

    @ManyToOne
    User user;

    @OneToMany(mappedBy = "cart")
    Set<CartItem> cartItems;
}
