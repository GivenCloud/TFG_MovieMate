package com.moviemate.dto.tmdb;

import java.util.List;

import lombok.Data;

@Data
public class TmdbTvDetails {
    private Integer id;
    private String name;
    private String overview;
    private String first_air_date;
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
