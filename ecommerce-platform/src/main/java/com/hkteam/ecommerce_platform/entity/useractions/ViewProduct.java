package com.hkteam.ecommerce_platform.entity.useractions;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

import com.hkteam.ecommerce_platform.entity.embed.ViewProductKey;
import com.hkteam.ecommerce_platform.entity.product.Product;
import com.hkteam.ecommerce_platform.entity.user.User;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class ViewProduct {
    @EmbeddedId
    ViewProductKey id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne
    @MapsId("productId")
    @JoinColumn(name = "product_id")
    Product product;

    @Min(0)
    int count;
}
