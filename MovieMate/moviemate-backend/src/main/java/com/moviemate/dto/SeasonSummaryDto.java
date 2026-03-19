package com.moviemate.dto;

import lombok.Data;

@Data
public class SeasonSummaryDto {
    private Integer seasonNumber;
    private String name;
    private String overview;
    private Integer episodeCount;
    private String posterUrl;
    private String airDate;
}
