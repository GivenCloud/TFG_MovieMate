package com.moviemate.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserStatsResponse {
    private Integer totalRatings;
    private Double averageRating;
    private Integer moviesWatched;
    private Integer seriesWatched;
    private Integer totalWatchTime;
    private Integer listsCreated;
    private Integer followersCount;
    private Integer followingCount;
    private Integer likesReceived;
}
