package com.moviemate.dto;

import lombok.Data;

@Data
public class EpisodeDto {
    private Integer episodeNumber;
    private String name;
    private String overview;
    private String airDate;
    private Integer runtime;
    private String stillUrl;
    private Double voteAverage;
}
