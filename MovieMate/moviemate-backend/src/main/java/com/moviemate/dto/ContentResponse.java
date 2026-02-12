package com.moviemate.dto;

import java.util.List;

import com.moviemate.entity.Content;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContentResponse {
    private Long id;
    private Integer tmdbId;
    private Content.ContentType contentType;
    private String title;
    private String releaseDate;
    private String synopsis;
    private String posterUrl;
    private String backdropUrl;
    private List<String> genres;
    private Double tmdbRating;
    private Integer tmdbVoteCount;
    private Double appRating;
    private Integer appVoteCount;
}