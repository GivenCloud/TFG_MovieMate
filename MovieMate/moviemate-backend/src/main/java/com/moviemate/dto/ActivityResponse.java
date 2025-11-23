package com.moviemate.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ActivityResponse {
    private String type; // "RATING", "LIST_CREATION", "FOLLOW"
    private UserResponse user;
    private LocalDateTime createdAt;
    private RatingResponse rating; // Si el tipo es RATING
    private ListResponse list; // Si el tipo es LIST_CREATION
    private UserResponse targetUser; // Si el tipo es FOLLOW
}