package com.hkteam.ecommerce_platform.entity.image;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import com.hkteam.ecommerce_platform.entity.useractions.Review;

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
public class ReviewImage extends Image {
    @ManyToOne
    Review review;
}
