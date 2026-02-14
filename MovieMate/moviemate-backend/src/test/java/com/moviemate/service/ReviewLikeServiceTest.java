package com.moviemate.service;

import com.moviemate.entity.Rating;
import com.moviemate.entity.ReviewLike;
import com.moviemate.entity.User;
import com.moviemate.repository.RatingRepository;
import com.moviemate.repository.ReviewLikeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ReviewLikeServiceTest {

    private ReviewLikeRepository reviewLikeRepository;
    private RatingRepository ratingRepository;
    private NotificationService notificationService;
    private ReviewLikeService reviewLikeService;

    @BeforeEach
    void setUp() {
        reviewLikeRepository = mock(ReviewLikeRepository.class);
        ratingRepository = mock(RatingRepository.class);
        notificationService = mock(NotificationService.class);
        
        reviewLikeService = new ReviewLikeService(
            reviewLikeRepository,
            ratingRepository,
            notificationService
        );
    }

    // ---------- toggleLike ----------

    @Test
    void toggleLike_shouldAddLike_whenNotLikedBefore() {
        User user = buildUser(1L, "chris");
        User ratingOwner = buildUser(2L, "alex");
        Rating rating = buildRating(10L, ratingOwner);

        when(ratingRepository.findById(10L)).thenReturn(Optional.of(rating));
        when(reviewLikeRepository.findByUserAndRating(user, rating))
                .thenReturn(Optional.empty());

        when(reviewLikeRepository.save(any(ReviewLike.class))).thenAnswer(i -> i.getArgument(0));

        boolean result = reviewLikeService.toggleLike(user, 10L);

        assertThat(result).isTrue();
        verify(reviewLikeRepository).save(any(ReviewLike.class));
        verify(notificationService).sendLikeNotification(eq(ratingOwner), any(ReviewLike.class));
        verify(reviewLikeRepository, never()).delete(any());
    }

    @Test
    void toggleLike_shouldRemoveLike_whenAlreadyLiked() {
        User user = buildUser(1L, "chris");
        Rating rating = buildRating(10L, buildUser(2L, "alex"));
        ReviewLike existingLike = buildReviewLike(1L, user, rating);

        when(ratingRepository.findById(10L)).thenReturn(Optional.of(rating));
        when(reviewLikeRepository.findByUserAndRating(user, rating))
                .thenReturn(Optional.of(existingLike));

        boolean result = reviewLikeService.toggleLike(user, 10L);

        assertThat(result).isFalse();
        verify(reviewLikeRepository).delete(existingLike);
        verify(reviewLikeRepository, never()).save(any());
        verify(notificationService, never()).sendLikeNotification(any(), any());
    }

    @Test
    void toggleLike_shouldThrow_whenRatingNotFound() {
        User user = buildUser(1L, "chris");

        when(ratingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewLikeService.toggleLike(user, 999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Review no encontrada");

        verify(reviewLikeRepository, never()).save(any());
        verify(reviewLikeRepository, never()).delete(any());
    }

    @Test
    void toggleLike_shouldSendNotification_whenAddingLike() {
        User user = buildUser(1L, "chris");
        User ratingOwner = buildUser(2L, "alex");
        Rating rating = buildRating(10L, ratingOwner);

        when(ratingRepository.findById(10L)).thenReturn(Optional.of(rating));
        when(reviewLikeRepository.findByUserAndRating(user, rating))
                .thenReturn(Optional.empty());

        when(reviewLikeRepository.save(any(ReviewLike.class))).thenAnswer(i -> i.getArgument(0));

        reviewLikeService.toggleLike(user, 10L);

        verify(notificationService).sendLikeNotification(eq(ratingOwner), any(ReviewLike.class));
    }

    // ---------- hasLiked ----------

    @Test
    void hasLiked_shouldReturnTrue_whenUserHasLiked() {
        User user = buildUser(1L, "chris");
        Rating rating = buildRating(10L, buildUser(2L, "alex"));

        when(ratingRepository.findById(10L)).thenReturn(Optional.of(rating));
        when(reviewLikeRepository.existsByUserAndRating(user, rating)).thenReturn(true);

        boolean result = reviewLikeService.hasLiked(user, 10L);

        assertThat(result).isTrue();
        verify(ratingRepository).findById(10L);
        verify(reviewLikeRepository).existsByUserAndRating(user, rating);
    }

    @Test
    void hasLiked_shouldReturnFalse_whenUserHasNotLiked() {
        User user = buildUser(1L, "chris");
        Rating rating = buildRating(10L, buildUser(2L, "alex"));

        when(ratingRepository.findById(10L)).thenReturn(Optional.of(rating));
        when(reviewLikeRepository.existsByUserAndRating(user, rating)).thenReturn(false);

        boolean result = reviewLikeService.hasLiked(user, 10L);

        assertThat(result).isFalse();
        verify(reviewLikeRepository).existsByUserAndRating(user, rating);
    }

    @Test
    void hasLiked_shouldThrow_whenRatingNotFound() {
        User user = buildUser(1L, "chris");

        when(ratingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewLikeService.hasLiked(user, 999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Review no encontrada");

        verify(reviewLikeRepository, never()).existsByUserAndRating(any(), any());
    }

    // ---------- getLikesCount ----------

    @Test
    void getLikesCount_shouldReturnCount_whenRatingExists() {
        Rating rating = buildRating(10L, buildUser(1L, "chris"));

        when(ratingRepository.findById(10L)).thenReturn(Optional.of(rating));
        when(reviewLikeRepository.countByRating(rating)).thenReturn(5);

        Integer count = reviewLikeService.getLikesCount(10L);

        assertThat(count).isEqualTo(5);
        verify(ratingRepository).findById(10L);
        verify(reviewLikeRepository).countByRating(rating);
    }

    @Test
    void getLikesCount_shouldReturnZero_whenNoLikes() {
        Rating rating = buildRating(10L, buildUser(1L, "chris"));

        when(ratingRepository.findById(10L)).thenReturn(Optional.of(rating));
        when(reviewLikeRepository.countByRating(rating)).thenReturn(0);

        Integer count = reviewLikeService.getLikesCount(10L);

        assertThat(count).isEqualTo(0);
    }

    @Test
    void getLikesCount_shouldThrow_whenRatingNotFound() {
        when(ratingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewLikeService.getLikesCount(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Review no encontrada");

        verify(reviewLikeRepository, never()).countByRating(any());
    }

    // ---------- getUserLikesCount ----------

    @Test
    void getUserLikesCount_shouldReturnCount() {
        User user = buildUser(1L, "chris");

        when(reviewLikeRepository.countByUser(user)).thenReturn(10);

        Integer count = reviewLikeService.getUserLikesCount(user);

        assertThat(count).isEqualTo(10);
        verify(reviewLikeRepository).countByUser(user);
    }

    @Test
    void getUserLikesCount_shouldReturnZero_whenUserHasNoLikes() {
        User user = buildUser(1L, "chris");

        when(reviewLikeRepository.countByUser(user)).thenReturn(0);

        Integer count = reviewLikeService.getUserLikesCount(user);

        assertThat(count).isEqualTo(0);
    }

    // ---------- helpers ----------

    private User buildUser(Long id, String username) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setEmail(username + "@example.com");
        return u;
    }

    private Rating buildRating(Long id, User user) {
        Rating r = new Rating();
        r.setId(id);
        r.setUser(user);
        r.setRating(5);
        return r;
    }

    private ReviewLike buildReviewLike(Long id, User user, Rating rating) {
        ReviewLike rl = new ReviewLike();
        rl.setId(id);
        rl.setUser(user);
        rl.setRating(rating);
        return rl;
    }
}