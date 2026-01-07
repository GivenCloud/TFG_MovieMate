package com.moviemate.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

import com.moviemate.entity.ActivityType;

@Data
@Builder
public class ActivityResponse {

    private ActivityType type;
    private UserResponse user;
    private LocalDateTime createdAt;
    
    // Solo uno de estos será no nulo dependiendo del tipo
    private RatingResponse rating;
    private ListResponse list;
    private UserResponse targetUser; // Para FOLLOW
    private ContentResponse content; // Para CONTENT_ADDED_TO_LIST
}