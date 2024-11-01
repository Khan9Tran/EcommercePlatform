package com.hkteam.ecommerce_platform.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.hkteam.ecommerce_platform.dto.response.PaginationResponse;
import com.hkteam.ecommerce_platform.dto.response.ProductResponse;
import com.hkteam.ecommerce_platform.repository.ProductElasticsearchRepository;
import com.hkteam.ecommerce_platform.util.PageUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ElasticSearchService {
    ProductElasticsearchRepository productElasticsearchRepository;

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

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // Get by category, brand, store
        if (categoryId != null) {
            boolQuery.filter(QueryBuilders.termQuery("categoryId", categoryId));
        }
        if (brandId != null) {
            boolQuery.filter(QueryBuilders.termQuery("brandId", brandId));
        }
        if (storeId != null) {
            boolQuery.filter(QueryBuilders.termQuery("storeId", storeId));
        }

        // isAvailable
        if (isAvailable != null) {
            boolQuery.filter(QueryBuilders.termQuery("isAvailable", isAvailable));
        }

        // isBlocked
        if (isBlocked != null) {
            boolQuery.filter(QueryBuilders.termQuery("isBlocked", isBlocked));
        }

        // Price range
        if (minPrice != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("salePrice").gte(minPrice));
        }
        if (maxPrice != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("salePrice").lte(maxPrice));
        }

        // Minimum rating
        if (minRate > 0) {
            boolQuery.filter(QueryBuilders.rangeQuery("rating").gte(minRate));
        }

        // Searching
        if (search != null) {
            boolQuery.must(QueryBuilders.multiMatchQuery(search, "name^3", "description^2", "details", "brandName", "categoryName")
                    .fuzziness(Fuzziness.AUTO)
                    .type(MultiMatchQueryBuilder.Type.BEST_FIELDS));
        }

        // Nested query
        if (search != null) {
            NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery(
                    "productComponentValues",
                    QueryBuilders.multiMatchQuery(search, "productComponentValues.value")
                            .fuzziness(Fuzziness.AUTO),
                    ScoreMode.Total
            );

            boolQuery.must(nestedQuery);
        }


        Sort sort;
        if (order.equals("asc"))
            sort = Sort.by(sortBy).ascending();
        else
            sort = Sort.by(sortBy).descending();

        Pageable pageable = PageUtils.createPageable(page, limit, sort);
        var result = productElasticsearchRepository.findAll(pageable);

        return  null;
    }

}

