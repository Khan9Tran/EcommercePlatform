package com.hkteam.ecommerce_platform.repository;

import java.util.List;


import com.hkteam.ecommerce_platform.entity.elasticsearch.ProductElasticsearch;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductElasticsearchRepository extends ElasticsearchRepository<ProductElasticsearch, String> {
    List<ProductElasticsearch> findByBrandId(Long brandId);

    List<ProductElasticsearch> findByCategoryId(Long categoryId);

    List<ProductElasticsearch> findByStoreId(String storeId);
}
