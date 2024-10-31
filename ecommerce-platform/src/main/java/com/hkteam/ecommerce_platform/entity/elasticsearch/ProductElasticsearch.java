package com.hkteam.ecommerce_platform.entity.elasticsearch;

import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "products")
public class ProductElasticsearch {
    String id;
    @Field(type = FieldType.Text)
    String slug;
    @Field(type = FieldType.Text)
    String name;
    @Field(type = FieldType.Text)
    String description;
    @Field(type = FieldType.Text)
    String details;
    String videoUrl;
    String mainImageUrl;

    @Field(type = FieldType.Double)
    BigDecimal originalPrice;
    @Field(type = FieldType.Double)
    BigDecimal salePrice;

    @Field(type = FieldType.Boolean)
    boolean isAvailable;

    int quantity;
    @Field(type = FieldType.Float)
    Float rating;

    @Field(type = FieldType.Text)
    String brandName;
    Long brandId;

    @Field(type = FieldType.Text)
    String categoryName;
    Long categoryId;

    @Field(type = FieldType.Text)
    String storeName;
    String storeId;

    @Field(type = FieldType.Date)
    Instant createdAt;

    @Field(type = FieldType.Date)
    Instant lastUpdatedAt;

    @Field(type = FieldType.Boolean)
    boolean isBlocked;

    @Field(type = FieldType.Text)
    List<String> productComponentValues;
}
