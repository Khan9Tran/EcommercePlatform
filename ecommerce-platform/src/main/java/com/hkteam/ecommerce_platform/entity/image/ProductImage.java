package com.hkteam.ecommerce_platform.entity.image;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import com.hkteam.ecommerce_platform.entity.product.Product;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@Entity
@NoArgsConstructor
public class ProductImage extends Image {
    @ManyToOne
    Product product;
}
