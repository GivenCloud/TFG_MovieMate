package com.moviemate.service;

import com.moviemate.dto.ActivityResponse;
import com.moviemate.entity.*;
import com.moviemate.entity.List;
import com.moviemate.repository.FollowerRepository;
import com.moviemate.repository.ListRepository;
import com.moviemate.repository.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ActivityServiceTest {

    private FollowerRepository followerRepository;
    private RatingRepository ratingRepository;
    private ListRepository listRepository;
    private RatingService ratingService;
    private ListService listService;
    private UserService userService;
    private ContentService contentService;
    private ActivityService activityService;

    @BeforeEach
    void setUp() {
        followerRepository = mock(FollowerRepository.class);
        ratingRepository = mock(RatingRepository.class);
        listRepository = mock(ListRepository.class);
        ratingService = mock(RatingService.class);
        listService = mock(ListService.class);
        userService = mock(UserService.class);
        contentService = mock(ContentService.class);

        activityService = new ActivityService(
            followerRepository,
            ratingRepository,
            listRepository,
            ratingService,
            listService,
            userService,
            contentService
        );
    }

    // ---------- getUserFeed ----------

    @Test
    void getUserFeed_shouldReturnActivities_whenUserFollowsOthers() {
        User currentUser = buildUser(1L, "chris");
        User followedUser = buildUser(2L, "alex");
        
        Follower follower = new Follower();
        follower.setFollower(currentUser);
        follower.setFollowed(followedUser);
        
        when(followerRepository.findByFollower(currentUser))
                .thenReturn(java.util.List.of(follower));

        // Actividades de ratings
        Rating rating = buildRating(10L, followedUser);
        Page<Rating> ratingsPage = new PageImpl<>(java.util.List.of(rating));
        when(ratingRepository.findByUserInOrderByCreatedAtDesc(
                any(), 
                any(PageRequest.class)))
                .thenReturn(ratingsPage);

        // Actividades de listas
        List list = buildList(20L, followedUser);
        Page<List> listsPage = new PageImpl<>(java.util.List.of(list));
        when(listRepository.findByUserInAndIsPublicTrueOrderByCreatedAtDesc(
                any(),
                any(PageRequest.class)))
                .thenReturn(listsPage);

        // Actividades de follows
        Follower recentFollow = new Follower();
        recentFollow.setFollower(followedUser);
        recentFollow.setFollowed(buildUser(3L, "sam"));
        recentFollow.setCreatedAt(LocalDateTime.now().minusDays(1));
        Page<Follower> followsPage = new PageImpl<>(java.util.List.of(recentFollow));
        when(followerRepository.findByFollowerInAndCreatedAtAfterOrderByCreatedAtDesc(
                any(),
                any(LocalDateTime.class),
                any(PageRequest.class)))
                .thenReturn(followsPage);

        // Mock mappers
        when(ratingService.mapToRatingResponse(rating))
                .thenReturn(mock(com.moviemate.dto.RatingResponse.class));
        when(listService.mapToListResponse(list))
                .thenReturn(mock(com.moviemate.dto.ListResponse.class));
        when(userService.mapToUserResponse(any(User.class)))
                .thenReturn(mock(com.moviemate.dto.UserResponse.class));

        Pageable pageable = PageRequest.of(0, 10);
        Page<ActivityResponse> result = activityService.getUserFeed(currentUser, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3); // 1 rating + 1 list + 1 follow
        assertThat(result.getTotalElements()).isEqualTo(3);

        verify(followerRepository).findByFollower(currentUser);
        verify(ratingRepository).findByUserInOrderByCreatedAtDesc(any(), any(PageRequest.class));
        verify(listRepository).findByUserInAndIsPublicTrueOrderByCreatedAtDesc(any(), any(PageRequest.class));
    }

    @Test
    void getUserFeed_shouldReturnEmpty_whenUserFollowsNobody() {
        User currentUser = buildUser(1L, "chris");
        
        when(followerRepository.findByFollower(currentUser))
                .thenReturn(Collections.emptyList());

        Pageable pageable = PageRequest.of(0, 10);
        Page<ActivityResponse> result = activityService.getUserFeed(currentUser, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);

        verify(followerRepository).findByFollower(currentUser);
        verify(ratingRepository, never()).findByUserInOrderByCreatedAtDesc(any(), any());
    }

    @Test
    void getUserFeed_shouldPaginateCorrectly_whenMultiplePages() {
        User currentUser = buildUser(1L, "chris");
        User followedUser = buildUser(2L, "alex");
        
        Follower follower = new Follower();
        follower.setFollower(currentUser);
        follower.setFollowed(followedUser);
        
        when(followerRepository.findByFollower(currentUser))
                .thenReturn(java.util.List.of(follower));

        // Crear múltiples ratings
        java.util.List<Rating> ratings = new java.util.ArrayList<>();
        for (int i = 0; i < 25; i++) {
            ratings.add(buildRating((long) i, followedUser));
        }
        Page<Rating> ratingsPage = new PageImpl<>(ratings);
        when(ratingRepository.findByUserInOrderByCreatedAtDesc(any(), any(PageRequest.class)))
                .thenReturn(ratingsPage);

        when(listRepository.findByUserInAndIsPublicTrueOrderByCreatedAtDesc(any(), any(PageRequest.class)))
                .thenReturn(Page.empty());
        when(followerRepository.findByFollowerInAndCreatedAtAfterOrderByCreatedAtDesc(any(), any(), any()))
                .thenReturn(Page.empty());

        when(ratingService.mapToRatingResponse(any()))
                .thenReturn(mock(com.moviemate.dto.RatingResponse.class));
        when(userService.mapToUserResponse(any()))
                .thenReturn(mock(com.moviemate.dto.UserResponse.class));

        // Primera página
        Pageable page0 = PageRequest.of(0, 10);
        Page<ActivityResponse> result0 = activityService.getUserFeed(currentUser, page0);
        assertThat(result0.getContent()).hasSize(10);
        assertThat(result0.getTotalElements()).isEqualTo(25);

        // Segunda página
        Pageable page1 = PageRequest.of(1, 10);
        Page<ActivityResponse> result1 = activityService.getUserFeed(currentUser, page1);
        assertThat(result1.getContent()).hasSize(10);
    }

    @Test
    void getUserFeed_shouldReturnEmpty_whenOffsetExceedsSize() {
        User currentUser = buildUser(1L, "chris");
        User followedUser = buildUser(2L, "alex");
        
        Follower follower = new Follower();
        follower.setFollower(currentUser);
        follower.setFollowed(followedUser);
        
        when(followerRepository.findByFollower(currentUser))
                .thenReturn(java.util.List.of(follower));

        when(ratingRepository.findByUserInOrderByCreatedAtDesc(any(), any()))
                .thenReturn(Page.empty());
        when(listRepository.findByUserInAndIsPublicTrueOrderByCreatedAtDesc(any(), any()))
                .thenReturn(Page.empty());
        when(followerRepository.findByFollowerInAndCreatedAtAfterOrderByCreatedAtDesc(any(), any(), any()))
                .thenReturn(Page.empty());

        Pageable pageable = PageRequest.of(10, 10); // Offset muy grande
        Page<ActivityResponse> result = activityService.getUserFeed(currentUser, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void getUserFeed_shouldSortByDateDescending() {
        User currentUser = buildUser(1L, "chris");
        User followedUser = buildUser(2L, "alex");
        
        Follower follower = new Follower();
        follower.setFollower(currentUser);
        follower.setFollowed(followedUser);
        
        when(followerRepository.findByFollower(currentUser))
                .thenReturn(java.util.List.of(follower));

        // Rating más antiguo
        Rating oldRating = buildRating(1L, followedUser);
        oldRating.setCreatedAt(LocalDateTime.now().minusDays(5));
        
        // Rating más reciente
        Rating newRating = buildRating(2L, followedUser);
        newRating.setCreatedAt(LocalDateTime.now().minusDays(1));

        Page<Rating> ratingsPage = new PageImpl<>(java.util.List.of(oldRating, newRating));
        when(ratingRepository.findByUserInOrderByCreatedAtDesc(any(), any()))
                .thenReturn(ratingsPage);

        when(listRepository.findByUserInAndIsPublicTrueOrderByCreatedAtDesc(any(), any()))
                .thenReturn(Page.empty());
        when(followerRepository.findByFollowerInAndCreatedAtAfterOrderByCreatedAtDesc(any(), any(), any()))
                .thenReturn(Page.empty());

        when(ratingService.mapToRatingResponse(any()))
                .thenReturn(mock(com.moviemate.dto.RatingResponse.class));
        when(userService.mapToUserResponse(any()))
                .thenReturn(mock(com.moviemate.dto.UserResponse.class));

        Pageable pageable = PageRequest.of(0, 10);
        Page<ActivityResponse> result = activityService.getUserFeed(currentUser, pageable);

        assertThat(result.getContent()).hasSize(2);
        // Verificar que están ordenados por fecha descendente
        assertThat(result.getContent().get(0).getCreatedAt())
                .isAfter(result.getContent().get(1).getCreatedAt());
    }

    // ---------- getGlobalActivity ----------

    @Test
    void getGlobalActivity_shouldReturnAllPublicActivities() {
        Pageable pageable = PageRequest.of(0, 10);

        Rating rating = buildRating(1L, buildUser(1L, "chris"));
        Page<Rating> ratingsPage = new PageImpl<>(java.util.List.of(rating));
        when(ratingRepository.findAllByOrderByCreatedAtDesc(pageable))
                .thenReturn(ratingsPage);

        List list = buildList(1L, buildUser(2L, "alex"));
        list.setIsPublic(true);
        Page<List> listsPage = new PageImpl<>(java.util.List.of(list));
        when(listRepository.findByIsPublicTrueOrderByCreatedAtDesc(pageable))
                .thenReturn(listsPage);

        when(ratingService.mapToRatingResponse(rating))
                .thenReturn(mock(com.moviemate.dto.RatingResponse.class));
        when(listService.mapToListResponse(list))
                .thenReturn(mock(com.moviemate.dto.ListResponse.class));
        when(userService.mapToUserResponse(any()))
                .thenReturn(mock(com.moviemate.dto.UserResponse.class));

        Page<ActivityResponse> result = activityService.getGlobalActivity(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2); // 1 rating + 1 list
        assertThat(result.getTotalElements()).isEqualTo(2);

        verify(ratingRepository).findAllByOrderByCreatedAtDesc(pageable);
        verify(listRepository).findByIsPublicTrueOrderByCreatedAtDesc(pageable);
    }

    @Test
    void getGlobalActivity_shouldReturnEmpty_whenNoActivities() {
        Pageable pageable = PageRequest.of(0, 10);

        when(ratingRepository.findAllByOrderByCreatedAtDesc(pageable))
                .thenReturn(Page.empty());
        when(listRepository.findByIsPublicTrueOrderByCreatedAtDesc(pageable))
                .thenReturn(Page.empty());

        Page<ActivityResponse> result = activityService.getGlobalActivity(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void getGlobalActivity_shouldPaginateCorrectly() {
        Pageable pageable = PageRequest.of(0, 5);

        java.util.List<Rating> ratings = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ratings.add(buildRating((long) i, buildUser((long) i, "user" + i)));
        }
        Page<Rating> ratingsPage = new PageImpl<>(ratings);
        when(ratingRepository.findAllByOrderByCreatedAtDesc(pageable))
                .thenReturn(ratingsPage);

        when(listRepository.findByIsPublicTrueOrderByCreatedAtDesc(pageable))
                .thenReturn(Page.empty());

        when(ratingService.mapToRatingResponse(any()))
                .thenReturn(mock(com.moviemate.dto.RatingResponse.class));
        when(userService.mapToUserResponse(any()))
                .thenReturn(mock(com.moviemate.dto.UserResponse.class));

        Page<ActivityResponse> result = activityService.getGlobalActivity(pageable);

        assertThat(result.getContent()).hasSizeLessThanOrEqualTo(5);
    }

    @Test
    void getGlobalActivity_shouldSortByDateDescending() {
        Pageable pageable = PageRequest.of(0, 10);

        Rating oldRating = buildRating(1L, buildUser(1L, "chris"));
        oldRating.setCreatedAt(LocalDateTime.now().minusDays(5));
        
        Rating newRating = buildRating(2L, buildUser(2L, "alex"));
        newRating.setCreatedAt(LocalDateTime.now().minusDays(1));

        Page<Rating> ratingsPage = new PageImpl<>(java.util.List.of(oldRating, newRating));
        when(ratingRepository.findAllByOrderByCreatedAtDesc(pageable))
                .thenReturn(ratingsPage);

        when(listRepository.findByIsPublicTrueOrderByCreatedAtDesc(pageable))
                .thenReturn(Page.empty());

        when(ratingService.mapToRatingResponse(any()))
                .thenReturn(mock(com.moviemate.dto.RatingResponse.class));
        when(userService.mapToUserResponse(any()))
                .thenReturn(mock(com.moviemate.dto.UserResponse.class));

        Page<ActivityResponse> result = activityService.getGlobalActivity(pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getCreatedAt())
                .isAfter(result.getContent().get(1).getCreatedAt());
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
        r.setCreatedAt(LocalDateTime.now().minusDays(2));
        return r;
    }

    private List buildList(Long id, User user) {
        List list = new List();
        list.setId(id);
        list.setUser(user);
        list.setName("Lista " + id);
        list.setIsPublic(true);
        list.setCreatedAt(LocalDateTime.now().minusDays(3));
        return list;
    }
}