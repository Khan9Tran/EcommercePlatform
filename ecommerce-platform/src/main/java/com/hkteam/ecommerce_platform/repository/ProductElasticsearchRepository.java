package com.hkteam.ecommerce_platform.repository;

import com.hkteam.ecommerce_platform.entity.elasticsearch.ProductElasticsearch;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ProductElasticsearchRepository extends ElasticsearchRepository<ProductElasticsearch, String> {
    List<ProductElasticsearch> findByBrandId(Long brandId);
    List<ProductElasticsearch> findByCategoryId(Long categoryId);
    Page<ProductElasticsearch> search(QueryBuilder queryBuilder, Pageable pageable);

}
