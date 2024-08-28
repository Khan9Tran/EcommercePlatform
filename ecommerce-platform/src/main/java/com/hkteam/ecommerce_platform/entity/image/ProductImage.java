package com.hkteam.ecommerce_platform.entity.image;

import com.hkteam.ecommerce_platform.entity.product.Product;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@Entity
@NoArgsConstructor
public class ProductImage extends Image{
    @ManyToOne
    Product product;
}
