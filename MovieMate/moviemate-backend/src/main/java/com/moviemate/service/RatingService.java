package com.moviemate.service;

import com.moviemate.dto.ContentResponse;
import com.moviemate.dto.RatingRequest;
import com.moviemate.dto.RatingResponse;
import com.moviemate.dto.UserResponse;
import com.moviemate.entity.Content;
import com.moviemate.entity.Rating;
import com.moviemate.entity.User;
import com.moviemate.repository.ContentRepository;
import com.moviemate.repository.RatingRepository;
import com.moviemate.repository.ReviewLikeRepository;
import com.moviemate.repository.UserRepository;
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
    private final ContentService contentService;
    private final UserRepository userRepository;
    private final ReviewLikeRepository reviewLikeRepository;

    public List<RatingResponse> getRatingsByContent(User currentUser, Long contentId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Contenido no encontrado"));
        return ratingRepository.findByContent(content).stream()
                .map(rating -> {
                    RatingResponse dto = mapToRatingResponse(rating);
                    int likes = reviewLikeRepository.countByRating(rating);
                    dto.setLikesCount(likes);
                    dto.setLikedByCurrentUser(
                        currentUser != null && reviewLikeRepository.existsByUserAndRating(currentUser, rating)
                    );
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public RatingResponse createOrUpdateRating(User user, RatingRequest request) {
        Content content = contentService.getOrFetch(request.getTmdbId());

        Rating rating = ratingRepository.findByUserAndContent(user, content)
            .orElse(new Rating());
            
        rating.setUser(user);
        rating.setContent(content);
        rating.setRating(request.getRating());
        rating.setReviewText(request.getReviewText());
        rating.setEmotionalTag(request.getEmotionalTag());
        rating.setStatus(request.getStatus());
        rating.setWatchedDate(request.getWatchedDate());
        rating.setContainsSpoiler(request.isContainsSpoiler());
        
        Rating savedRating = ratingRepository.save(rating);
        
        // Actualizar estadísticas del contenido
        updateContentStatistics(content);
        contentService.refreshAsync(content.getId());
        
        return mapToRatingResponse(savedRating);
    }

    public void deleteRating(User user, Long ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new RuntimeException("Valoración no encontrada"));

        if (!rating.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("No tienes permisos para modificar esta valoración");
        }

        Content content = rating.getContent();
        ratingRepository.delete(rating);

        updateContentStatistics(content);
    }

    public RatingResponse getRatingById(Long ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new RuntimeException("Valoración no encontrada"));
        return mapToRatingResponse(rating);
    }

    public User getRatingAuthor(Long ratingId) {
        return ratingRepository.findById(ratingId)
                .orElseThrow(() -> new RuntimeException("Valoración no encontrada"))
                .getUser();
    }

    public void adminDeleteRating(Long ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new RuntimeException("Valoración no encontrada"));
        Content content = rating.getContent();
        ratingRepository.delete(rating);
        updateContentStatistics(content);
    }
    
    private void updateContentStatistics(Content content) {
        Integer voteCount = ratingRepository.countRatingsByContent(content.getId());
        Double averageRating = ratingRepository.calculateAverageRatingByContent(content.getId());

        content.setAppRating(averageRating != null ? averageRating : 0.0);
        content.setAppVoteCount(voteCount != null ? voteCount : 0);

        contentRepository.save(content);
    }

    @Transactional
    public List<RatingResponse> getUserRatingsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return getUserRatings(user);
    }

    @Transactional
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
            .containsSpoiler(rating.isContainsSpoiler())
            .createdAt(rating.getCreatedAt())
            .user(UserResponse.builder()
                .id(rating.getUser().getId())
                .username(rating.getUser().getUsername())
                .avatarUrl(rating.getUser().getAvatarUrl())
                .build())
            .content(ContentResponse.builder()
                .id(rating.getContent().getId())
                .tmdbId(rating.getContent().getTmdbId())
                .title(rating.getContent().getTitle())
                .contentType(rating.getContent().getContentType())
                .releaseDate(rating.getContent().getReleaseDate() != null ? rating.getContent().getReleaseDate().toString() : null)
                .posterUrl(rating.getContent().getPosterUrl())
                .backdropUrl(rating.getContent().getBackdropUrl())
                .synopsis(rating.getContent().getSynopsis())
                .genres(rating.getContent().getGenres())
                .tmdbRating(rating.getContent().getTmdbRating())
                .tmdbVoteCount(rating.getContent().getTmdbVoteCount())
                .appRating(rating.getContent().getAppRating())
                .appVoteCount(rating.getContent().getAppVoteCount())
                .build())
            .build();
    }
}