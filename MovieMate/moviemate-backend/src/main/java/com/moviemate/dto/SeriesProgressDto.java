package com.moviemate.dto;

public record SeriesProgressDto(
        Integer tmdbSeriesId,
        String title,
        String posterUrl,
        long watchedCount
) {}
