package com.hkteam.ecommerce_platform.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.json.JsonData;
import lombok.experimental.UtilityClass;

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

    public BoolQuery createSearchProducts(
            List<Long> categoryIds,
            List<Long> brandIds,
            String storeId,
            String search,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int minRate) {
        List<Query> queries = new ArrayList<>();

        if (categoryIds != null && !categoryIds.isEmpty()) {
            List<FieldValue> categoryFieldValues =
                    categoryIds.stream().map(FieldValue::of).collect(Collectors.toList());
            queries.add(Query.of(
                    q -> q.terms(t -> t.field("categoryId").terms(terms -> terms.value(categoryFieldValues)))));
        }
        if (brandIds != null && !brandIds.isEmpty()) {
            List<FieldValue> brandFieldValues =
                    brandIds.stream().map(FieldValue::of).collect(Collectors.toList());
            queries.add(Query.of(q -> q.terms(t -> t.field("brandId").terms(terms -> terms.value(brandFieldValues)))));
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

        BoolQuery boolQuery = new BoolQuery.Builder()
                .should(Query.of(q -> q.multiMatch(m -> m.fields(List.of(
                                "name",
                                "description",
                                "details",
                                "brandName",
                                "categoryName",
                                "storeName",
                                "componentValues.value"))
                        .query(search)
                        .fuzziness("AUTO")
                        .operator(Operator.Or)
                        .analyzer("standard"))))
                .filter(queries)
                .build();


        return boolQuery;
    }

    public Supplier<Query> createSupplierSearchProducts(
            List<Long> categoryId,
            List<Long> brandIds,
            String storeId,
            String search,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int minRate) {
        return () -> Query.of(
                q -> q.bool(createSearchProducts(categoryId, brandIds, storeId, search, minPrice, maxPrice, minRate)));
    }
}
