package com.moviemate.dto.tmdb;

import lombok.Data;

@Data
public class SearchResult {
    private Long id;
    private String title;      // Movie
    private String name;       // TV  
    private String mediaType;  // "movie" | "tv" 
    private String releaseDate;
    private String firstAirDate;
}
