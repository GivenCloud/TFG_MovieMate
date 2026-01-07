package com.moviemate.dto;

import com.moviemate.entity.Content;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContentResponse {
    private Long id;
    private Integer tmdbId;
    private String title;
    private Content.ContentType contentType;
    private String releaseDate;
    private String posterUrl;
    private String backdropUrl;
    private String synopsis;
    private String[] genres;
    private Double averageRating;
    private Integer voteCount;
    private String lastSync;
}