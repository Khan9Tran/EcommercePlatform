package com.hkteam.ecommerce_platform.repository;

import java.util.List;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.hkteam.ecommerce_platform.entity.elasticsearch.ProductElasticsearch;

public interface ProductElasticsearchRepository extends ElasticsearchRepository<ProductElasticsearch, String> {
    List<ProductElasticsearch> findByBrandId(Long brandId);

    List<ProductElasticsearch> findByCategoryId(Long categoryId);

    List<ProductElasticsearch> findByStoreId(String storeId);
}
