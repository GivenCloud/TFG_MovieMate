package com.moviemate.service;

import com.moviemate.dto.BadgeDto;
import com.moviemate.entity.User;
import com.moviemate.entity.UserBadge;
import com.moviemate.entity.UserStats;
import com.moviemate.repository.UserBadgeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class BadgeServiceTest {

    private UserBadgeRepository userBadgeRepository;
    private BadgeService badgeService;

    @BeforeEach
    void setUp() {
        userBadgeRepository = mock(UserBadgeRepository.class);
        badgeService = new BadgeService(userBadgeRepository);
    }

    // ---------- getUserBadges ----------

    @Test
    void getUserBadges_shouldReturnMappedDtos() {
        User user = buildUser(1L, "alice");
        UserBadge badge = buildBadge(user, "FIRST_REVIEW");

        when(userBadgeRepository.findByUser(user)).thenReturn(List.of(badge));

        List<BadgeDto> result = badgeService.getUserBadges(user);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo("FIRST_REVIEW");
        assertThat(result.get(0).getName()).isEqualTo("Primera valoración");
        assertThat(result.get(0).getDescription()).isNotBlank();
        assertThat(result.get(0).getIcon()).isNotBlank();
        verify(userBadgeRepository).findByUser(user);
    }

    @Test
    void getUserBadges_shouldReturnEmpty_whenNoBadges() {
        User user = buildUser(1L, "alice");
        when(userBadgeRepository.findByUser(user)).thenReturn(List.of());

        List<BadgeDto> result = badgeService.getUserBadges(user);

        assertThat(result).isEmpty();
    }

    @Test
    void getUserBadges_shouldHandleUnknownBadgeType() {
        User user = buildUser(1L, "alice");
        UserBadge badge = buildBadge(user, "UNKNOWN_BADGE");

        when(userBadgeRepository.findByUser(user)).thenReturn(List.of(badge));

        List<BadgeDto> result = badgeService.getUserBadges(user);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo("UNKNOWN_BADGE");
        assertThat(result.get(0).getName()).isEqualTo("UNKNOWN_BADGE"); // fallback
    }

    // ---------- checkAndAward — award conditions ----------

    @Test
    void checkAndAward_shouldAwardFirstReview_whenOneRating() {
        User user = buildUser(1L, "alice");
        UserStats stats = buildStats(1, 0, 0, 0, 0, 0);

        when(userBadgeRepository.existsByUserAndBadgeType(eq(user), any())).thenReturn(false);

        badgeService.checkAndAward(user, stats);

        verify(userBadgeRepository).save(argThat(b -> b.getBadgeType().equals("FIRST_REVIEW")));
    }

    @Test
    void checkAndAward_shouldAwardCritic_whenTenRatings() {
        User user = buildUser(1L, "alice");
        UserStats stats = buildStats(10, 0, 0, 0, 0, 0);

        when(userBadgeRepository.existsByUserAndBadgeType(eq(user), any())).thenReturn(false);

        badgeService.checkAndAward(user, stats);

        verify(userBadgeRepository).save(argThat(b -> b.getBadgeType().equals("CRITIC")));
        verify(userBadgeRepository).save(argThat(b -> b.getBadgeType().equals("FIRST_REVIEW")));
    }

    @Test
    void checkAndAward_shouldAwardMovieMarathon_whenTenMovies() {
        User user = buildUser(1L, "alice");
        UserStats stats = buildStats(10, 10, 0, 0, 0, 0);

        when(userBadgeRepository.existsByUserAndBadgeType(eq(user), any())).thenReturn(false);

        badgeService.checkAndAward(user, stats);

        verify(userBadgeRepository).save(argThat(b -> b.getBadgeType().equals("MOVIE_MARATHON")));
    }

    @Test
    void checkAndAward_shouldAwardSeriesBinge_whenTenSeries() {
        User user = buildUser(1L, "alice");
        UserStats stats = buildStats(10, 0, 10, 0, 0, 0);

        when(userBadgeRepository.existsByUserAndBadgeType(eq(user), any())).thenReturn(false);

        badgeService.checkAndAward(user, stats);

        verify(userBadgeRepository).save(argThat(b -> b.getBadgeType().equals("SERIES_BINGE")));
    }

    @Test
    void checkAndAward_shouldAwardSocial_whenOneFollower() {
        User user = buildUser(1L, "alice");
        UserStats stats = buildStats(0, 0, 0, 1, 0, 0);

        when(userBadgeRepository.existsByUserAndBadgeType(eq(user), any())).thenReturn(false);

        badgeService.checkAndAward(user, stats);

        verify(userBadgeRepository).save(argThat(b -> b.getBadgeType().equals("SOCIAL")));
    }

    @Test
    void checkAndAward_shouldAwardPopular_whenTenFollowers() {
        User user = buildUser(1L, "alice");
        UserStats stats = buildStats(0, 0, 0, 10, 0, 0);

        when(userBadgeRepository.existsByUserAndBadgeType(eq(user), any())).thenReturn(false);

        badgeService.checkAndAward(user, stats);

        verify(userBadgeRepository).save(argThat(b -> b.getBadgeType().equals("POPULAR")));
        verify(userBadgeRepository).save(argThat(b -> b.getBadgeType().equals("SOCIAL")));
    }

    @Test
    void checkAndAward_shouldAwardLister_whenOneList() {
        User user = buildUser(1L, "alice");
        UserStats stats = buildStats(0, 0, 0, 0, 1, 0);

        when(userBadgeRepository.existsByUserAndBadgeType(eq(user), any())).thenReturn(false);

        badgeService.checkAndAward(user, stats);

        verify(userBadgeRepository).save(argThat(b -> b.getBadgeType().equals("LISTER")));
    }

    @Test
    void checkAndAward_shouldAwardLiked_whenOneLike() {
        User user = buildUser(1L, "alice");
        UserStats stats = buildStats(0, 0, 0, 0, 0, 1);

        when(userBadgeRepository.existsByUserAndBadgeType(eq(user), any())).thenReturn(false);

        badgeService.checkAndAward(user, stats);

        verify(userBadgeRepository).save(argThat(b -> b.getBadgeType().equals("LIKED")));
    }

    @Test
    void checkAndAward_shouldNotAward_whenConditionNotMet() {
        User user = buildUser(1L, "alice");
        UserStats stats = buildStats(0, 0, 0, 0, 0, 0);

        badgeService.checkAndAward(user, stats);

        verify(userBadgeRepository, never()).save(any());
    }

    @Test
    void checkAndAward_shouldNotDuplicate_whenBadgeAlreadyExists() {
        User user = buildUser(1L, "alice");
        UserStats stats = buildStats(1, 0, 0, 0, 0, 0);

        when(userBadgeRepository.existsByUserAndBadgeType(user, "FIRST_REVIEW")).thenReturn(true);

        badgeService.checkAndAward(user, stats);

        verify(userBadgeRepository, never()).save(any());
    }

    @Test
    void checkAndAward_shouldHandleNullStats() {
        User user = buildUser(1L, "alice");
        UserStats stats = new UserStats(); // all fields null

        when(userBadgeRepository.existsByUserAndBadgeType(eq(user), any())).thenReturn(false);

        // Should not throw — nulls are handled with ternary defaults
        badgeService.checkAndAward(user, stats);

        verify(userBadgeRepository, never()).save(any());
    }

    // ---------- helpers ----------

    private User buildUser(Long id, String username) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        return u;
    }

    private UserBadge buildBadge(User user, String type) {
        UserBadge b = new UserBadge();
        b.setId(1L);
        b.setUser(user);
        b.setBadgeType(type);
        b.setAwardedAt(LocalDateTime.now());
        return b;
    }

    private UserStats buildStats(int ratings, int movies, int series, int followers, int lists, int likes) {
        UserStats s = new UserStats();
        s.setTotalRatings(ratings);
        s.setMoviesWatched(movies);
        s.setSeriesWatched(series);
        s.setFollowersCount(followers);
        s.setListsCreated(lists);
        s.setLikesReceived(likes);
        return s;
    }
}
