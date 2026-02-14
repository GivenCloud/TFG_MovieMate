package com.moviemate.service;

import com.moviemate.dto.UserStatsResponse;
import com.moviemate.entity.*;
import com.moviemate.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserStatsServiceTest {

    private UserStatsRepository userStatsRepository;
    private UserRepository userRepository;
    private RatingRepository ratingRepository;
    private ListRepository listRepository;
    private FollowerRepository followerRepository;
    private ReviewLikeRepository reviewLikeRepository;
    private UserStatsService userStatsService;

    @BeforeEach
    void setUp() {
        userStatsRepository = mock(UserStatsRepository.class);
        userRepository = mock(UserRepository.class);
        ratingRepository = mock(RatingRepository.class);
        listRepository = mock(ListRepository.class);
        followerRepository = mock(FollowerRepository.class);
        reviewLikeRepository = mock(ReviewLikeRepository.class);
        
        userStatsService = new UserStatsService(
            userStatsRepository,
            userRepository,
            ratingRepository,
            listRepository,
            followerRepository,
            reviewLikeRepository
        );
    }

    // ---------- getOrCreateAndUpdateStats ----------

    @Test
    void getOrCreateAndUpdateStats_shouldUpdateAndReturn_whenUserExists() {
        User user = buildUser(1L, "chris");
        UserStats stats = buildUserStats(user);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userStatsRepository.findByUser(user)).thenReturn(Optional.of(stats));
        when(ratingRepository.findByUser(user)).thenReturn(List.of());
        when(listRepository.countByUser(user)).thenReturn(0);
        when(followerRepository.countByFollowed(user)).thenReturn(0);
        when(followerRepository.countByFollower(user)).thenReturn(0);
        when(userStatsRepository.save(any(UserStats.class))).thenReturn(stats);

        UserStatsResponse response = userStatsService.getOrCreateAndUpdateStats(1L);

        assertThat(response).isNotNull();
        verify(userRepository).findById(1L);
        verify(userStatsRepository).save(any(UserStats.class));
    }

    @Test
    void getOrCreateAndUpdateStats_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userStatsService.getOrCreateAndUpdateStats(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");

        verify(userStatsRepository, never()).save(any());
    }

    // ---------- updateUserStats ----------

    @Test
    void updateUserStats_shouldCalculateCorrectly_whenUserHasRatings() {
        User user = buildUser(1L, "chris");
        UserStats stats = buildUserStats(user);

        Content movie1 = buildContent(100, Content.ContentType.MOVIE);
        Content movie2 = buildContent(101, Content.ContentType.MOVIE);
        Content tv1 = buildContent(200, Content.ContentType.TV);

        Rating rating1 = buildRating(1L, user, movie1, 5, Rating.Status.VISTA);
        Rating rating2 = buildRating(2L, user, movie2, 3, Rating.Status.VISTA);
        Rating rating3 = buildRating(3L, user, tv1, 4, Rating.Status.VISTA);

        when(userStatsRepository.findByUser(user)).thenReturn(Optional.of(stats));
        when(ratingRepository.findByUser(user)).thenReturn(List.of(rating1, rating2, rating3));
        when(listRepository.countByUser(user)).thenReturn(5);
        when(followerRepository.countByFollowed(user)).thenReturn(10);
        when(followerRepository.countByFollower(user)).thenReturn(15);
        when(reviewLikeRepository.countByRating(rating1)).thenReturn(2);
        when(reviewLikeRepository.countByRating(rating2)).thenReturn(3);
        when(reviewLikeRepository.countByRating(rating3)).thenReturn(1);
        when(userStatsRepository.save(any(UserStats.class))).thenReturn(stats);

        UserStatsResponse response = userStatsService.updateUserStats(user);

        assertThat(response.getTotalRatings()).isEqualTo(3);
        assertThat(response.getAverageRating()).isEqualTo(4.0); // (5+3+4)/3
        assertThat(response.getMoviesWatched()).isEqualTo(2);
        assertThat(response.getSeriesWatched()).isEqualTo(1);
        assertThat(response.getListsCreated()).isEqualTo(5);
        assertThat(response.getFollowersCount()).isEqualTo(10);
        assertThat(response.getFollowingCount()).isEqualTo(15);
        assertThat(response.getLikesReceived()).isEqualTo(6); // 2+3+1
        assertThat(response.getTotalWatchTime()).isEqualTo(285); // (2*120)+(1*45)
    }

    @Test
    void updateUserStats_shouldCreateNew_whenStatsNotExist() {
        User user = buildUser(1L, "chris");

        when(userStatsRepository.findByUser(user)).thenReturn(Optional.empty());
        when(ratingRepository.findByUser(user)).thenReturn(List.of());
        when(listRepository.countByUser(user)).thenReturn(0);
        when(followerRepository.countByFollowed(user)).thenReturn(0);
        when(followerRepository.countByFollower(user)).thenReturn(0);

        UserStats newStats = buildUserStats(user);
        when(userStatsRepository.save(any(UserStats.class))).thenReturn(newStats);

        UserStatsResponse response = userStatsService.updateUserStats(user);

        assertThat(response).isNotNull();
        verify(userStatsRepository).save(any(UserStats.class));
    }

    @Test
    void updateUserStats_shouldReturnZeroAverage_whenNoRatings() {
        User user = buildUser(1L, "chris");
        UserStats stats = buildUserStats(user);

        when(userStatsRepository.findByUser(user)).thenReturn(Optional.of(stats));
        when(ratingRepository.findByUser(user)).thenReturn(List.of());
        when(listRepository.countByUser(user)).thenReturn(0);
        when(followerRepository.countByFollowed(user)).thenReturn(0);
        when(followerRepository.countByFollower(user)).thenReturn(0);
        when(userStatsRepository.save(any(UserStats.class))).thenReturn(stats);

        UserStatsResponse response = userStatsService.updateUserStats(user);

        assertThat(response.getTotalRatings()).isEqualTo(0);
        assertThat(response.getAverageRating()).isEqualTo(0.0);
        assertThat(response.getMoviesWatched()).isEqualTo(0);
        assertThat(response.getSeriesWatched()).isEqualTo(0);
    }

    @Test
    void updateUserStats_shouldOnlyCountVistaStatus() {
        User user = buildUser(1L, "chris");
        UserStats stats = buildUserStats(user);

        Content movie = buildContent(100, Content.ContentType.MOVIE);
        Rating vistaRating = buildRating(1L, user, movie, 5, Rating.Status.VISTA);
        Rating pendienteRating = buildRating(2L, user, movie, 4, Rating.Status.POR_VER);

        when(userStatsRepository.findByUser(user)).thenReturn(Optional.of(stats));
        when(ratingRepository.findByUser(user)).thenReturn(List.of(vistaRating, pendienteRating));
        when(listRepository.countByUser(user)).thenReturn(0);
        when(followerRepository.countByFollowed(user)).thenReturn(0);
        when(followerRepository.countByFollower(user)).thenReturn(0);
        when(reviewLikeRepository.countByRating(any())).thenReturn(0);
        when(userStatsRepository.save(any(UserStats.class))).thenReturn(stats);

        UserStatsResponse response = userStatsService.updateUserStats(user);

        assertThat(response.getTotalRatings()).isEqualTo(2); // Cuenta ambos
        assertThat(response.getMoviesWatched()).isEqualTo(1); // Solo VISTA
    }

    @Test
    void updateUserStats_shouldCalculateWatchTime_correctly() {
        User user = buildUser(1L, "chris");
        UserStats stats = buildUserStats(user);

        Content movie1 = buildContent(100, Content.ContentType.MOVIE);
        Content movie2 = buildContent(101, Content.ContentType.MOVIE);
        Content tv1 = buildContent(200, Content.ContentType.TV);
        Content tv2 = buildContent(201, Content.ContentType.TV);

        Rating r1 = buildRating(1L, user, movie1, 5, Rating.Status.VISTA);
        Rating r2 = buildRating(2L, user, movie2, 4, Rating.Status.VISTA);
        Rating r3 = buildRating(3L, user, tv1, 5, Rating.Status.VISTA);
        Rating r4 = buildRating(4L, user, tv2, 4, Rating.Status.VISTA);

        when(userStatsRepository.findByUser(user)).thenReturn(Optional.of(stats));
        when(ratingRepository.findByUser(user)).thenReturn(List.of(r1, r2, r3, r4));
        when(listRepository.countByUser(user)).thenReturn(0);
        when(followerRepository.countByFollowed(user)).thenReturn(0);
        when(followerRepository.countByFollower(user)).thenReturn(0);
        when(reviewLikeRepository.countByRating(any())).thenReturn(0);
        when(userStatsRepository.save(any(UserStats.class))).thenReturn(stats);

        UserStatsResponse response = userStatsService.updateUserStats(user);

        // 2 películas * 120 min + 2 series * 45 min = 240 + 90 = 330 min
        assertThat(response.getTotalWatchTime()).isEqualTo(330);
    }

    // ---------- incrementListsCount ----------

    @Test
    void incrementListsCount_shouldIncrement_whenStatsExist() {
        User user = buildUser(1L, "chris");
        UserStats stats = buildUserStats(user);
        stats.setListsCreated(5);

        when(userStatsRepository.findById(1L)).thenReturn(Optional.of(stats));
        when(userStatsRepository.save(stats)).thenReturn(stats);

        userStatsService.incrementListsCount(user);

        assertThat(stats.getListsCreated()).isEqualTo(6);
        verify(userStatsRepository).save(stats);
    }

    @Test
    void incrementListsCount_shouldCreateAndIncrement_whenStatsNotExist() {
        User user = buildUser(1L, "chris");

        when(userStatsRepository.findById(1L)).thenReturn(Optional.empty());

        UserStats newStats = buildUserStats(user);
        when(userStatsRepository.save(any(UserStats.class))).thenReturn(newStats);

        userStatsService.incrementListsCount(user);

        verify(userStatsRepository).save(any(UserStats.class));
    }

    // ---------- incrementRatingsCount ----------

    @Test
    void incrementRatingsCount_shouldIncrementAndRecalculate_whenStatsExist() {
        User user = buildUser(1L, "chris");
        UserStats stats = buildUserStats(user);
        stats.setTotalRatings(5);
        stats.setAverageRating(4.0);

        Content movie = buildContent(100, Content.ContentType.MOVIE);
        Rating rating1 = buildRating(1L, user, movie, 5, Rating.Status.VISTA);
        Rating rating2 = buildRating(2L, user, movie, 3, Rating.Status.VISTA);

        when(userStatsRepository.findById(1L)).thenReturn(Optional.of(stats));
        when(ratingRepository.findByUser(user)).thenReturn(List.of(rating1, rating2));
        when(userStatsRepository.save(stats)).thenReturn(stats);

        userStatsService.incrementRatingsCount(user);

        assertThat(stats.getTotalRatings()).isEqualTo(6);
        assertThat(stats.getAverageRating()).isEqualTo(4.0); // (5+3)/2
        verify(userStatsRepository).save(stats);
    }

    @Test
    void incrementRatingsCount_shouldCreateAndIncrement_whenStatsNotExist() {
        User user = buildUser(1L, "chris");

        when(userStatsRepository.findById(1L)).thenReturn(Optional.empty());
        when(ratingRepository.findByUser(user)).thenReturn(List.of());

        UserStats newStats = buildUserStats(user);
        when(userStatsRepository.save(any(UserStats.class))).thenReturn(newStats);

        userStatsService.incrementRatingsCount(user);

        verify(userStatsRepository).save(any(UserStats.class));
    }

    // ---------- mapToUserStatsResponse ----------

    @Test
    void mapToUserStatsResponse_shouldMapAllFields() {
        User user = buildUser(1L, "chris");
        UserStats stats = buildUserStats(user);
        stats.setTotalRatings(10);
        stats.setAverageRating(4.5);
        stats.setMoviesWatched(5);
        stats.setSeriesWatched(3);
        stats.setTotalWatchTime(600);
        stats.setListsCreated(2);
        stats.setFollowersCount(20);
        stats.setFollowingCount(15);
        stats.setLikesReceived(50);

        UserStatsResponse response = userStatsService.mapToUserStatsResponse(stats);

        assertThat(response.getTotalRatings()).isEqualTo(10);
        assertThat(response.getAverageRating()).isEqualTo(4.5);
        assertThat(response.getMoviesWatched()).isEqualTo(5);
        assertThat(response.getSeriesWatched()).isEqualTo(3);
        assertThat(response.getTotalWatchTime()).isEqualTo(600);
        assertThat(response.getListsCreated()).isEqualTo(2);
        assertThat(response.getFollowersCount()).isEqualTo(20);
        assertThat(response.getFollowingCount()).isEqualTo(15);
        assertThat(response.getLikesReceived()).isEqualTo(50);
    }

    // ---------- helpers ----------

    private User buildUser(Long id, String username) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        return u;
    }

    private UserStats buildUserStats(User user) {
        UserStats stats = new UserStats();
        stats.setUser(user);
        stats.setTotalRatings(0);
        stats.setAverageRating(0.0);
        stats.setMoviesWatched(0);
        stats.setSeriesWatched(0);
        stats.setTotalWatchTime(0);
        stats.setListsCreated(0);
        stats.setFollowersCount(0);
        stats.setFollowingCount(0);
        stats.setLikesReceived(0);
        return stats;
    }

    private Content buildContent(Integer tmdbId, Content.ContentType type) {
        Content c = new Content();
        c.setTmdbId(tmdbId);
        c.setContentType(type);
        c.setTitle("Content " + tmdbId);
        return c;
    }

    private Rating buildRating(Long id, User user, Content content, Integer rating, Rating.Status status) {
        Rating r = new Rating();
        r.setId(id);
        r.setUser(user);
        r.setContent(content);
        r.setRating(rating);
        r.setStatus(status);
        return r;
    }
}