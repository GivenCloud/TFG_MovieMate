package com.moviemate.dto;

import com.moviemate.entity.Rating;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class RatingResponse {
    private Long id;
    private Integer rating;
    private String reviewText;
    private Rating.EmotionalTag emotionalTag;
    private Rating.Status status;
    private LocalDate watchedDate;
    private LocalDateTime createdAt;
    private UserResponse user;
    private ContentResponse content;
    private Integer likesCount;
    private Boolean likedByCurrentUser;
    private Boolean containsSpoiler;
}