package com.moviemate.controller;

import com.moviemate.dto.BadgeDto;
import com.moviemate.dto.ChangePasswordRequest;
import com.moviemate.dto.ContentResponse;
import com.moviemate.dto.FullStatsDto;
import com.moviemate.dto.ListResponse;
import com.moviemate.dto.RatingResponse;
import com.moviemate.dto.UpdateProfileRequest;
import com.moviemate.dto.UpdateUserPublicStatusRequest;
import com.moviemate.dto.UserProfileResponse;
import com.moviemate.dto.UserResponse;
import com.moviemate.dto.UserStatsResponse;
import com.moviemate.entity.Content;
import com.moviemate.entity.User;
import com.moviemate.security.CustomUserDetails;
import com.moviemate.service.BadgeService;
import com.moviemate.service.ContentService;
import com.moviemate.service.FollowRequestService;
import com.moviemate.service.FollowerService;
import com.moviemate.service.ListService;
import com.moviemate.service.NotificationService;
import com.moviemate.service.RatingService;
import com.moviemate.service.TmdbService;
import com.moviemate.service.UserService;
import com.moviemate.service.UserStatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserControllerTest {

    private UserService userService;
    private FollowerService followerService;
    private UserStatsService userStatsService;
    private ListService listService;
    private RatingService ratingService;
    private FollowRequestService followRequestService;
    private NotificationService notificationService;
    private TmdbService tmdbService;
    private ContentService contentService;
    private BadgeService badgeService;
    private UserController userController;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        followerService = mock(FollowerService.class);
        userStatsService = mock(UserStatsService.class);
        listService = mock(ListService.class);
        ratingService = mock(RatingService.class);
        followRequestService = mock(FollowRequestService.class);
        notificationService = mock(NotificationService.class);
        tmdbService = mock(TmdbService.class);
        contentService = mock(ContentService.class);
        badgeService = mock(BadgeService.class);
        userController = new UserController(
                userService,
                followerService,
                userStatsService,
                listService,
                ratingService,
                followRequestService,
                notificationService,
                tmdbService,
                contentService,
                badgeService
        );
    }

    @Test
    void getCurrentUser_shouldReturnOk() {
        User user = buildUser(1L, "ana");
        UserResponse mapped = UserResponse.builder().id(1L).username("ana").build();
        when(userService.mapToUserResponse(user)).thenReturn(mapped);

        ResponseEntity<UserResponse> response = userController.getCurrentUser(new CustomUserDetails(user));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mapped);
    }

    @Test
    void getUserByUsername_shouldReturnOk() {
        User user = buildUser(2L, "bob");
        when(userService.findUserByUsername("bob")).thenReturn(user);
        when(userService.mapToUserResponse(user)).thenReturn(UserResponse.builder().id(2L).username("bob").build());

        ResponseEntity<UserResponse> response = userController.getUserByUsername("bob");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getUsername()).isEqualTo("bob");
    }

    @Test
    void getUserProfile_shouldReturnOk() {
        User target = buildUser(2L, "bob");
        target.setBio("Bio");
        target.setIsPublic(true);
        target.setCreatedAt(LocalDateTime.now());
        User current = buildUser(1L, "ana");

        when(userService.findUserById(2L)).thenReturn(target);
        when(followerService.getFollowersCount(target)).thenReturn(3);
        when(followerService.getFollowingCount(target)).thenReturn(5);
        when(followerService.isFollowing(current, target)).thenReturn(true);

        ResponseEntity<UserProfileResponse> response = userController.getUserProfile(new CustomUserDetails(current), 2L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getUsername()).isEqualTo("bob");
        assertThat(response.getBody().getIsFollowing()).isTrue();
    }

    @Test
    void updateProfile_shouldReturnOk() {
        User user = buildUser(1L, "ana");
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setBio("Nueva bio");
        request.setAvatarUrl("avatar.png");
        UserResponse mapped = UserResponse.builder().id(1L).username("ana").build();
        when(userService.updateUserProfile(user, "Nueva bio", "avatar.png")).thenReturn(mapped);

        ResponseEntity<UserResponse> response = userController.updateProfile(new CustomUserDetails(user), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mapped);
    }

    @Test
    void updatePublicStatus_shouldReturnOk() {
        User user = buildUser(1L, "ana");
        UpdateUserPublicStatusRequest request = new UpdateUserPublicStatusRequest();
        request.setIsPublic(false);
        UserResponse mapped = UserResponse.builder().id(1L).username("ana").build();
        when(userService.updateUserPublicStatus(user, false)).thenReturn(mapped);

        ResponseEntity<UserResponse> response = userController.updatePublicStatus(new CustomUserDetails(user), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mapped);
    }

    @Test
    void uploadAvatar_shouldHandleOkAndErrors() throws Exception {
        User user = buildUser(1L, "ana");
        CustomUserDetails userDetails = new CustomUserDetails(user);
        MultipartFile file = new MockMultipartFile("file", "a.png", "image/png", new byte[]{1});
        UserResponse mapped = UserResponse.builder().id(1L).username("ana").build();
        doReturn(mapped).when(userService).uploadAvatar(user, file);

        ResponseEntity<UserResponse> ok = userController.uploadAvatar(userDetails, file);
        assertThat(ok.getStatusCode()).isEqualTo(HttpStatus.OK);

        doThrow(new IllegalArgumentException("bad")).when(userService).uploadAvatar(user, file);
        ResponseEntity<UserResponse> bad = userController.uploadAvatar(userDetails, file);
        assertThat(bad.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        doThrow(new RuntimeException("boom")).when(userService).uploadAvatar(user, file);
        ResponseEntity<UserResponse> error = userController.uploadAvatar(userDetails, file);
        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void changePassword_shouldReturnNoContent() {
        User user = buildUser(1L, "ana");
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("old");
        request.setNewPassword("new123456");

        ResponseEntity<Void> response = userController.changePassword(new CustomUserDetails(user), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(userService).changePassword(user, "old", "new123456");
    }

    @Test
    void searchUsers_shouldHandleNullAndAuthUser() {
        when(userService.searchUsers(null, "an")).thenReturn(List.of());
        ResponseEntity<List<UserResponse>> nullPrincipal = userController.searchUsers(null, "an");
        assertThat(nullPrincipal.getStatusCode()).isEqualTo(HttpStatus.OK);

        User user = buildUser(1L, "ana");
        when(userService.searchUsers(1L, "an")).thenReturn(List.of(UserResponse.builder().id(2L).username("anita").build()));
        ResponseEntity<List<UserResponse>> authPrincipal = userController.searchUsers(new CustomUserDetails(user), "an");
        assertThat(authPrincipal.getBody()).hasSize(1);
    }

    @Test
    void getMeCollectionsAndSuggestions_shouldReturnOk() {
        User user = buildUser(1L, "ana");
        CustomUserDetails principal = new CustomUserDetails(user);

        when(listService.getUserLists(user)).thenReturn(List.of(ListResponse.builder().id(1L).build()));
        when(ratingService.getUserRatings(user)).thenReturn(List.of(RatingResponse.builder().id(1L).build()));
        when(followRequestService.findByReceiver(user)).thenReturn(List.of());
        when(notificationService.getNotifications(user)).thenReturn(List.of());
        when(userService.getSuggestedUsers(1L)).thenReturn(List.of(UserResponse.builder().id(2L).username("bob").build()));

        assertThat(userController.getUserLists(principal).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(userController.getUserRatings(principal).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(userController.getMyRequests(principal).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(userController.getMyNotifications(principal).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(userController.getSuggestedUsers(principal).getBody()).hasSize(1);
    }

    @Test
    void getMyRecommendations_shouldHandleEmptyAndGenreBasedFlow() {
        User user = buildUser(1L, "ana");
        CustomUserDetails principal = new CustomUserDetails(user);

        FullStatsDto emptyStats = new FullStatsDto();
        emptyStats.setTopGenres(new ArrayList<>());
        when(userStatsService.getFullStats(user)).thenReturn(emptyStats);
        ResponseEntity<List<ContentResponse>> emptyResponse = userController.getMyRecommendations(principal);
        assertThat(emptyResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(emptyResponse.getBody()).isEmpty();

        FullStatsDto richStats = new FullStatsDto();
        richStats.setTopGenres(List.of(new FullStatsDto.GenreStatDto("Accion", 10L)));
        when(userStatsService.getFullStats(user)).thenReturn(richStats);
        when(tmdbService.getMovieGenres()).thenReturn(List.of(new com.moviemate.dto.GenreDto(28, "Accion")));
        when(tmdbService.getTvGenres()).thenReturn(List.of(new com.moviemate.dto.GenreDto(18, "Drama")));
        Content movie = new Content();
        when(tmdbService.discoverMovies(28, null, 7.0, "vote_average.desc", 1)).thenReturn(List.of(movie));
        when(contentService.mapToContentResponse(any(Content.class))).thenReturn(ContentResponse.builder().id(10L).title("M").build());

        ResponseEntity<List<ContentResponse>> richResponse = userController.getMyRecommendations(principal);
        assertThat(richResponse.getBody()).hasSize(1);
    }

    @Test
    void getStatsAndPublicCollections_shouldReturnOk() {
        User user = buildUser(1L, "ana");
        CustomUserDetails principal = new CustomUserDetails(user);

        FullStatsDto fullStats = new FullStatsDto();
        when(userStatsService.getFullStats(user)).thenReturn(fullStats);
        when(userStatsService.getOrCreateAndUpdateStats(2L)).thenReturn(UserStatsResponse.builder().totalRatings(1).build());
        when(ratingService.getUserRatingsByUserId(2L)).thenReturn(List.of(RatingResponse.builder().id(9L).build()));
        when(listService.getListsByUserId(2L)).thenReturn(List.of(ListResponse.builder().id(8L).build()));

        assertThat(userController.getMyFullStats(principal).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(userController.getUserStats(2L).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(userController.getUserRatingsById(2L).getBody()).hasSize(1);
        assertThat(userController.getUserListsById(2L).getBody()).hasSize(1);
    }

    @Test
    void getBadges_shouldReturnOk() {
        User user = buildUser(1L, "ana");
        CustomUserDetails principal = new CustomUserDetails(user);
        BadgeDto badge = new BadgeDto();
        badge.setName("Primera reseña");
        when(badgeService.getUserBadges(user)).thenReturn(List.of(badge));

        User target = buildUser(2L, "bob");
        when(userService.findUserById(2L)).thenReturn(target);
        when(badgeService.getUserBadges(target)).thenReturn(List.of(badge));

        assertThat(userController.getMyBadges(principal).getBody()).hasSize(1);
        assertThat(userController.getUserBadges(2L).getBody()).hasSize(1);
    }

    private User buildUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@mail.com");
        return user;
    }
}
