package com.hkteam.ecommerce_platform.util;

import co.elastic.clients.elasticsearch._types.analysis.Analyzer;
import co.elastic.clients.elasticsearch._types.analysis.AnalyzerBuilders;
import co.elastic.clients.elasticsearch._types.analysis.AnalyzerVariant;
import co.elastic.clients.elasticsearch._types.analysis.LanguageAnalyzer;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
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
        Supplier<Query> supplier = ()-> Query.of(q->q.multiMatch(createAutoSuggestMatchQuery(partialProductName)));
        return  supplier;
    }
}
