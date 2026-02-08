package com.moviemate.service;

import com.moviemate.entity.ReviewLike;
import com.moviemate.entity.User;
import com.moviemate.entity.Rating;
import com.moviemate.repository.ReviewLikeRepository;
import com.moviemate.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewLikeService {

    private final ReviewLikeRepository reviewLikeRepository;
    private final RatingRepository ratingRepository;
    private final NotificationService notificationService;

    @Transactional
    public boolean toggleLike(User user, Long ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new RuntimeException("Review no encontrada"));

        ReviewLike existingLike = reviewLikeRepository.findByUserAndRating(user, rating)
                .orElse(null);

        if (existingLike != null) {
            // Quitar like
            reviewLikeRepository.delete(existingLike);
            return false;
        } else {
            // Dar like
            ReviewLike reviewLike = new ReviewLike();
            reviewLike.setUser(user);
            reviewLike.setRating(rating);
            reviewLikeRepository.save(reviewLike);

            notificationService.sendLikeNotification(rating.getUser(), reviewLike);

            return true;
        }
    }

    @Transactional(readOnly = true)
    public boolean hasLiked(User user, Long ratingId) {
        return ratingRepository.findById(ratingId)
                .map(rating -> reviewLikeRepository.existsByUserAndRating(user, rating))
                .orElseThrow(() -> new RuntimeException("Review no encontrada"));
    }

    @Transactional(readOnly = true)
    public Integer getLikesCount(Long ratingId) {
        return ratingRepository.findById(ratingId)
                .map(reviewLikeRepository::countByRating)
                .orElseThrow(() -> new RuntimeException("Review no encontrada"));
    }

    @Transactional(readOnly = true)
    public Integer getUserLikesCount(User user) {
        return reviewLikeRepository.countByUser(user);
    }
}