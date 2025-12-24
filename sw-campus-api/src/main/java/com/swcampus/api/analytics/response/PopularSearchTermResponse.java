package com.swcampus.api.analytics.response;

import java.util.List;

import com.swcampus.domain.analytics.PopularSearchTerm;

public record PopularSearchTermResponse(
    String term,
    long count
) {
    public static PopularSearchTermResponse from(PopularSearchTerm term) {
        return new PopularSearchTermResponse(
            term.term(),
            term.count()
        );
    }
    
    public static List<PopularSearchTermResponse> fromList(List<PopularSearchTerm> terms) {
        return terms.stream().map(PopularSearchTermResponse::from).toList();
    }
}
