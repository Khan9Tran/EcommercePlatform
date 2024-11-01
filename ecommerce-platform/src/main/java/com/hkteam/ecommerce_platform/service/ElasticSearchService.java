package com.hkteam.ecommerce_platform.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.hkteam.ecommerce_platform.dto.response.PaginationResponse;
import com.hkteam.ecommerce_platform.dto.response.ProductResponse;
import com.hkteam.ecommerce_platform.util.PageUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ElasticSearchService {
    ElasticsearchTemplate elasticsearchTemplate;

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

}

