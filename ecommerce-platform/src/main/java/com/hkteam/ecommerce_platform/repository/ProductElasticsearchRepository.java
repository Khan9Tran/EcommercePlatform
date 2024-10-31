package com.hkteam.ecommerce_platform.repository;

import com.hkteam.ecommerce_platform.entity.elasticsearch.ProductElasticsearch;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;
import java.util.Optional;


public interface ProductElasticsearchRepository extends ElasticsearchRepository<ProductElasticsearch, String> {
    List<ProductElasticsearch> findByBrandId(Long brandId);
}
