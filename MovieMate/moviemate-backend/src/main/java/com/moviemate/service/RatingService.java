package com.moviemate.service;

import com.moviemate.dto.ContentSimpleResponse;
import com.moviemate.dto.RatingRequest;
import com.moviemate.dto.RatingResponse;
import com.moviemate.dto.UserResponse;
import com.moviemate.entity.Content;
import com.moviemate.entity.Rating;
import com.moviemate.entity.User;
import com.moviemate.repository.ContentRepository;
import com.moviemate.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingService {
    
    private final RatingRepository ratingRepository;
    private final ContentRepository contentRepository;
    
    @Transactional
    public RatingResponse createOrUpdateRating(User user, RatingRequest request) {
        Content content = contentRepository.findById(request.getContentId())
            .orElseThrow(() -> new RuntimeException("Contenido no encontrado"));
            
        Rating rating = ratingRepository.findByUserAndContent(user, content)
            .orElse(new Rating());
            
        rating.setUser(user);
        rating.setContent(content);
        rating.setRating(request.getRating());
        rating.setReviewText(request.getReviewText());
        rating.setEmotionalTag(request.getEmotionalTag());
        rating.setStatus(request.getStatus());
        rating.setWatchedDate(request.getWatchedDate());
        
        Rating savedRating = ratingRepository.save(rating);
        
        // Actualizar estadísticas del contenido
        updateContentStatistics(content);
        
        return mapToRatingResponse(savedRating);
    }
    
    private void updateContentStatistics(Content content) {
        Double averageRating = ratingRepository.calculateAverageRatingByContent(content.getId());
        Integer voteCount = ratingRepository.countRatingsByContent(content.getId());
        
        content.setAverageRating(averageRating != null ? averageRating : 0.0);
        content.setVoteCount(voteCount);
        contentRepository.save(content);
    }
    
    public List<RatingResponse> getUserRatings(User user) {
        return ratingRepository.findByUser(user).stream()
            .map(this::mapToRatingResponse)
            .collect(Collectors.toList());
    }
    
    public RatingResponse mapToRatingResponse(Rating rating) {
        return RatingResponse.builder()
            .id(rating.getId())
            .rating(rating.getRating())
            .reviewText(rating.getReviewText())
            .emotionalTag(rating.getEmotionalTag())
            .status(rating.getStatus())
            .watchedDate(rating.getWatchedDate())
            .createdAt(rating.getCreatedAt())
            .user(UserResponse.builder()
                .id(rating.getUser().getId())
                .username(rating.getUser().getUsername())
                .avatarUrl(rating.getUser().getAvatarUrl())
                .build())
            .content(ContentSimpleResponse.builder()
                .id(rating.getContent().getId())
                .title(rating.getContent().getTitle())
                .posterUrl(rating.getContent().getPosterUrl())
                .contentType(rating.getContent().getContentType())
                .build())
            .build();
    }
}