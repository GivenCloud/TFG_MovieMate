package com.moviemate.dto.tmdb;

import java.util.List;

import lombok.Data;

@Data
public class MultiSearchResult {
    private List<SearchResult> results;
    private Integer totalResults;
    private Integer totalPages;
}
