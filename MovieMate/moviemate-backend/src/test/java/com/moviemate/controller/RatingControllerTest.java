package com.moviemate.controller;

import com.moviemate.dto.RatingRequest;
import com.moviemate.dto.RatingResponse;
import com.moviemate.entity.User;
import com.moviemate.security.CustomUserDetails;
import com.moviemate.service.RatingService;
import com.moviemate.service.ReviewLikeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RatingControllerTest {

    private RatingService ratingService;
    private ReviewLikeService reviewLikeService;
    private RatingController ratingController;

    @BeforeEach
    void setUp() {
        ratingService = mock(RatingService.class);
        reviewLikeService = mock(ReviewLikeService.class);
        ratingController = new RatingController(ratingService, reviewLikeService);
    }

    @Test
    void createOrUpdateRating_shouldReturnOk() {
        User user = buildUser(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);
        RatingRequest request = new RatingRequest();
        request.setTmdbId(1000);
        RatingResponse payload = RatingResponse.builder().id(10L).rating(5).build();
        when(ratingService.createOrUpdateRating(user, request)).thenReturn(payload);

        ResponseEntity<RatingResponse> response = ratingController.createOrUpdateRating(userDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(payload);
    }

    @Test
    void getRatingsByContent_shouldReturnOk() {
        User user = buildUser(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);
        List<RatingResponse> payload = List.of(RatingResponse.builder().id(10L).rating(4).build());
        when(ratingService.getRatingsByContent(user, 50L)).thenReturn(payload);

        ResponseEntity<List<RatingResponse>> response = ratingController.getRatingsByContent(userDetails, 50L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void deleteRating_shouldReturnNoContent() {
        User user = buildUser(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        ResponseEntity<Void> response = ratingController.deleteRating(userDetails, 10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(ratingService).deleteRating(user, 10L);
    }

    @Test
    void toggleLike_shouldReturnOk() {
        User user = buildUser(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        ResponseEntity<Void> response = ratingController.toggleLike(userDetails, 10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(reviewLikeService).toggleLike(user, 10L);
    }

    @Test
    void getLikesCount_shouldReturnOk() {
        when(reviewLikeService.getLikesCount(10L)).thenReturn(7);

        ResponseEntity<Integer> response = ratingController.getLikesCount(10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(7);
    }

    @Test
    void hasLiked_shouldReturnOk() {
        User user = buildUser(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);
        when(reviewLikeService.hasLiked(user, 10L)).thenReturn(true);

        ResponseEntity<Boolean> response = ratingController.hasLiked(userDetails, 10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isTrue();
    }

    private User buildUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("u" + id);
        return user;
    }
}
