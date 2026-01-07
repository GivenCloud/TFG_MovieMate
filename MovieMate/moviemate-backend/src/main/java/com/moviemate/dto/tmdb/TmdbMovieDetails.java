package com.moviemate.dto.tmdb;

import lombok.Data;
import java.util.List;

@Data
public class TmdbMovieDetails {
    private Integer id;
    private String title;
    private String overview;
    private String release_date;
    private String poster_path;
    private String backdrop_path;
    private Double vote_average;
    private Integer vote_count;
    private List<Genre> genres;

    @Data
    public static class Genre {
        private Integer id;
        private String name;
    }
}