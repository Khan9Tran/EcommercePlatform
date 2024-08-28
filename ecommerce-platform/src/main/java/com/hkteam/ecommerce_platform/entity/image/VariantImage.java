package com.hkteam.ecommerce_platform.entity.image;

import com.hkteam.ecommerce_platform.entity.product.Variant;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@Entity
@NoArgsConstructor
public class VariantImage extends Image{
    @ManyToOne
    Variant variant;
}
