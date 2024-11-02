package com.hkteam.ecommerce_platform.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.hkteam.ecommerce_platform.dto.response.PaginationResponse;
import com.hkteam.ecommerce_platform.dto.response.ProductResponse;
import com.hkteam.ecommerce_platform.dto.response.UserResponse;
import com.hkteam.ecommerce_platform.entity.elasticsearch.ProductElasticsearch;
import com.hkteam.ecommerce_platform.mapper.ProductMapper;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ElasticSearchService {
    private final ProductMapper productMapper;
    static final Set<String> ALLOWED_SORT_FIELDS = Set.of("salePrice", "rating", "name", "createdDate");
    static final Set<String> ALLOWED_SORT_ORDERS = Set.of("asc", "desc");
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
            int minRate) {

        int[] pageAndSize = PageUtils.validateAndConvertPageAndSize(page, limit);
        int pageInt = pageAndSize[0];
        int sizeInt = pageAndSize[1];
        int fromInt = (pageInt - 1) * sizeInt;

        List<SortOptions> sortOptions = new ArrayList<>();

        if (sortBy != null && ALLOWED_SORT_FIELDS.contains(sortBy)) {
            if (order == null || !ALLOWED_SORT_ORDERS.contains(order)) {
                order = "asc";
            }

            SortOrder sortOrder = order.equalsIgnoreCase("asc") ? SortOrder.Asc : SortOrder.Desc;
            FieldSort fieldSort = FieldSort.of(s -> s.field(sortBy).order(sortOrder));
            sortOptions.add(SortOptions.of(s -> s.field(fieldSort)));
        }


        Supplier<Query> supplier = ESUtils.createSupplierSearchProducts(
                categoryId, brandId, storeId, search, minPrice, maxPrice, minRate
        );
        try {
            SearchResponse<ProductElasticsearch> searchResponse = elasticsearchClient.search(
                    s -> s.index("products")
                            .query(supplier.get())
                            .from(fromInt)
                            .size(sizeInt)
                            .sort(sortOptions)
                    , ProductElasticsearch.class
            );

            log.info("es search query {}", supplier.get().toString());

            List<Hit<ProductElasticsearch>> hitList = searchResponse.hits().hits();
            List<ProductElasticsearch> productList = new ArrayList<>();

            for (Hit<ProductElasticsearch> hit : hitList) {
                productList.add(hit.source());
            }

            List<ProductResponse> productResponses = productList.stream()
                    .map(productMapper::toProductResponse)
                    .collect(Collectors.toList());

            // Tính toán các thông tin phân trang
            long totalElements = searchResponse.hits().total().value();  // Tổng số bản ghi
            int totalPages = (int) Math.ceil((double) totalElements / sizeInt); // Tổng số trang
            boolean hasNext = pageInt < totalPages;  // Có trang tiếp theo không
            boolean hasPrevious = pageInt > 1;

            return PaginationResponse.<ProductResponse>builder()
                    .currentPage(pageInt)
                    .pageSize(sizeInt)
                    .totalPages(totalPages)
                    .totalElements(totalElements)
                    .hasNext(hasNext)
                    .hasPrevious(hasPrevious)
                    .nextPage(hasNext ? pageInt + 1  : null)
                    .previousPage(hasPrevious ? pageInt - 1 : null)
                    .data(productResponses)
                    .build();
        } catch (Exception e) {
            log.error("Failed to parse Elasticsearch response: ", e);
            log.error("Response content: {}", e.getMessage());
        }
        return  null;
    }

    public List<String> autoSuggestionProduct(String search) throws IOException {
        Supplier<Query> supplier = ESUtils.createSupplierAutoSuggest(search);
        Set<String> uniqueProductNames = new HashSet<>(); // Sử dụng Set để loại bỏ trùng lặp
        try {
            SearchResponse<ProductElasticsearch> searchResponse = elasticsearchClient.search(
                    s -> s.index("products").query(supplier.get()).from(0).size(10), ProductElasticsearch.class
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

