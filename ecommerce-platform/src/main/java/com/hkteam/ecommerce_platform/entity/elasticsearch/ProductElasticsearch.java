package com.hkteam.ecommerce_platform.entity.elasticsearch;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Id;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "products")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductElasticsearch {
    @Id
    @Field(type = FieldType.Keyword)
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

    @Field(type = FieldType.Keyword)
    Long brandId;

    @Field(type = FieldType.Text)
    String categoryName;

    @Field(type = FieldType.Keyword)
    Long categoryId;

    @Field(type = FieldType.Text)
    String storeName;

    @Field(type = FieldType.Keyword)
    String storeId;

    @Field(type = FieldType.Date)
    Instant createdAt;

    @Field(type = FieldType.Date)
    Instant lastUpdatedAt;

    @Field(type = FieldType.Boolean)
    boolean isBlocked;

    @Field(type = FieldType.Nested)
    List<EsProComponentValue> productComponentValues;
}
