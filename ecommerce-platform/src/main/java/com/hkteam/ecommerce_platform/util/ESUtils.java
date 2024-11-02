package com.hkteam.ecommerce_platform.util;

import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.json.JsonData;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@UtilityClass
public class ESUtils {
    private static final Logger log = LoggerFactory.getLogger(ESUtils.class);

    public MultiMatchQuery createAutoSuggestMatchQuery(String partialProductName) {
        return new MultiMatchQuery.Builder()
                .fields(List.of("name", "description"))
                .query(partialProductName)
                .fuzziness("AUTO")
                .operator(Operator.Or)
                .analyzer("standard")
                .build();
    }

    public Supplier<Query> createSupplierAutoSuggest(String partialProductName) {
        Supplier<Query> supplier = () -> Query.of(q -> q.multiMatch(createAutoSuggestMatchQuery(partialProductName)));
        return supplier;
    }


    public BoolQuery createSearchProducts(Long categoryId,
                                          Long brandId,
                                          String storeId,
                                          String search,
                                          BigDecimal minPrice,
                                          BigDecimal maxPrice,
                                          int minRate) {
        List<Query> queries = new ArrayList<>();

        if (categoryId != null) {
            queries.add(Query.of(q -> q.term(t -> t.field("categoryId").value(categoryId))));
        }
        if (brandId != null) {
            queries.add(Query.of(q -> q.term(t -> t.field("brandId").value(brandId))));
        }
        if (storeId != null) {
            queries.add(Query.of(q -> q.term(t -> t.field("storeId").value(storeId))));
        }
        if (minPrice != null) {
            queries.add(Query.of(q -> q.range(r -> r.field("salePrice").gte(JsonData.of(minPrice)))));
        }
        if (maxPrice != null) {
            queries.add(Query.of(q -> q.range(r -> r.field("salePrice").lte(JsonData.of(maxPrice)))));
        }
        if (minRate > 0) {
            queries.add(Query.of(q -> q.range(r -> r.field("rating").gte(JsonData.of(minRate)))));
        }

        queries.add(Query.of(q -> q.term(t -> t.field("isAvailable").value(true))));
        queries.add(Query.of(q -> q.term(t -> t.field("isBlocked").value(false))));


        BoolQuery boolQuery = new BoolQuery.Builder().should(Query.of(q -> q.multiMatch(
                m -> m.fields(List.of("name", "description", "details", "brandName", "categoryName", "storeName","componentValues.value"))
                        .query(search)
                        .fuzziness("AUTO")
                        .operator(Operator.Or)
                        .analyzer("standard")
        ))).filter(queries).build();


        return  boolQuery;
    }

    public Supplier<Query> createSupplierSearchProducts(Long categoryId,
                                                        Long brandId,
                                                        String storeId,
                                                        String search,
                                                        BigDecimal minPrice,
                                                        BigDecimal maxPrice,
                                                        int minRate) {
        return  () -> Query.of(q -> q.bool(createSearchProducts(categoryId, brandId, storeId, search, minPrice, maxPrice, minRate)));
    }
}
