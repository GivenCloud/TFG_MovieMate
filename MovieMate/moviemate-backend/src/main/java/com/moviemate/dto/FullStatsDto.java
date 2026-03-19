package com.moviemate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class FullStatsDto {

    // ── Resumen básico ──────────────────────────────────────────
    private Integer totalRatings;
    private Double  averageRating;
    private Integer moviesWatched;
    private Integer seriesWatched;
    private Integer totalWatchTime;   // minutos
    private Integer listsCreated;
    private Integer followersCount;
    private Integer followingCount;
    private Integer likesReceived;

    // ── Distribución de notas (1-5) ─────────────────────────────
    private List<RatingCountDto> ratingDistribution;

    // ── Top géneros ─────────────────────────────────────────────
    private List<GenreStatDto> topGenres;

    // ── Actividad mensual (últimos 12 meses) ────────────────────
    private List<MonthlyActivityDto> monthlyActivity;

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class RatingCountDto {
        private Integer rating;
        private Long    count;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class GenreStatDto {
        private String genre;
        private Long   count;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class MonthlyActivityDto {
        private Integer year;
        private Integer month;
        private Long    count;
    }
}
