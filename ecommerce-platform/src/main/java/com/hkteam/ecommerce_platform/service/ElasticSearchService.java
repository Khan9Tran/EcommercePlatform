package com.hkteam.ecommerce_platform.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.hkteam.ecommerce_platform.dto.response.PaginationResponse;
import com.hkteam.ecommerce_platform.dto.response.ProductResponse;
import com.hkteam.ecommerce_platform.entity.elasticsearch.ProductElasticsearch;
import com.hkteam.ecommerce_platform.util.ESUtils;
import com.hkteam.ecommerce_platform.util.PageUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ElasticSearchService {
    ElasticsearchClient elasticsearchClient;

    public PaginationResponse<ProductResponse> getAllProducts(
            Long categoryId,
            Long brandId,
            String storeId,
            String sortBy,
            String order,
            String page,
            String limit,
            String search,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int minRate,
            Boolean isAvailable,
            Boolean isBlocked) {
        return null;
    }

    public List<String> autoSuggestionProduct(String search) throws IOException {
        Supplier<Query> supplier = ESUtils.createSupplierAutoSuggest(search);
        Set<String> uniqueProductNames = new HashSet<>(); // Sử dụng Set để loại bỏ trùng lặp
        try {
            SearchResponse<ProductElasticsearch> searchResponse = elasticsearchClient.search(
                    s -> s.index("products").query(supplier.get()), ProductElasticsearch.class
            );

            log.info("es auto suggest query {}", supplier.get().toString());

            List<Hit<ProductElasticsearch>> hitList = searchResponse.hits().hits();
            List<ProductElasticsearch> productList = new ArrayList<>();

            for (Hit<ProductElasticsearch> hit : hitList) {
                productList.add(hit.source());
            }

            for (ProductElasticsearch product : productList) {
                uniqueProductNames.add(product.getName()); // Thêm vào Set để loại bỏ trùng lặp
            }
        } catch (Exception e) {
            log.error("Failed to parse Elasticsearch response: ", e);
            log.error("Response content: {}", e.getMessage());
        }

        return new ArrayList<>(uniqueProductNames); // Chuyển đổi Set trở lại List
    }


}

