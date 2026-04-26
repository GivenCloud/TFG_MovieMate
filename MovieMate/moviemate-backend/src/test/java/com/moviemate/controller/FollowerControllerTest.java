package com.moviemate.controller;

import com.moviemate.dto.FollowRequestActionResponse;
import com.moviemate.dto.UserResponse;
import com.moviemate.entity.User;
import com.moviemate.security.CustomUserDetails;
import com.moviemate.service.FollowRequestService;
import com.moviemate.service.FollowerService;
import com.moviemate.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class FollowerControllerTest {

    private FollowerService followerService;
    private UserService userService;
    private FollowRequestService followRequestService;
    private FollowerController followerController;

    @BeforeEach
    void setUp() {
        followerService = mock(FollowerService.class);
        userService = mock(UserService.class);
        followRequestService = mock(FollowRequestService.class);
        followerController = new FollowerController(followerService, userService, followRequestService);
    }

    @Test
    void sendFollowRequest_shouldReturnOk() {
        User current = buildUser(1L);
        User target = buildUser(2L);
        CustomUserDetails userDetails = new CustomUserDetails(current);
        when(userService.findUserById(2L)).thenReturn(target);

        ResponseEntity<Void> response = followerController.sendFollowRequest(userDetails, 2L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(followRequestService).sendFollowRequest(current, target);
    }

    @Test
    void acceptFollowRequest_shouldReturnOk() {
        User current = buildUser(1L);
        CustomUserDetails userDetails = new CustomUserDetails(current);
        FollowRequestActionResponse payload = new FollowRequestActionResponse(
                10L, 2L, 1L, FollowRequestActionResponse.FollowRequestStatus.ACCEPTED, LocalDateTime.now());
        when(followRequestService.acceptRequest(10L, current)).thenReturn(payload);

        ResponseEntity<FollowRequestActionResponse> response = followerController.acceptFollowRequest(userDetails, 10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(payload);
    }

    @Test
    void rejectFollowRequest_shouldReturnOk() {
        User current = buildUser(1L);
        CustomUserDetails userDetails = new CustomUserDetails(current);
        FollowRequestActionResponse payload = new FollowRequestActionResponse(
                10L, 2L, 1L, FollowRequestActionResponse.FollowRequestStatus.REJECTED, LocalDateTime.now());
        when(followRequestService.rejectRequest(10L, current)).thenReturn(payload);

        ResponseEntity<FollowRequestActionResponse> response = followerController.rejectFollowRequest(userDetails, 10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(payload);
    }

    @Test
    void unfollowUser_shouldReturnNoContent() {
        User current = buildUser(1L);
        User target = buildUser(2L);
        CustomUserDetails userDetails = new CustomUserDetails(current);
        when(userService.findUserById(2L)).thenReturn(target);

        ResponseEntity<Void> response = followerController.unfollowUser(userDetails, 2L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(followerService).unfollowUser(current, target);
    }

    @Test
    void getFollowers_shouldReturnOk() {
        List<UserResponse> payload = List.of(UserResponse.builder().id(2L).username("ana").build());
        when(followerService.getFollowers(2L)).thenReturn(payload);

        ResponseEntity<List<UserResponse>> response = followerController.getFollowers(2L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void getFollowing_shouldReturnOk() {
        List<UserResponse> payload = List.of(UserResponse.builder().id(3L).username("bob").build());
        when(followerService.getFollowing(2L)).thenReturn(payload);

        ResponseEntity<List<UserResponse>> response = followerController.getFollowing(2L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void isFollowing_shouldReturnOk() {
        User current = buildUser(1L);
        User target = buildUser(2L);
        CustomUserDetails userDetails = new CustomUserDetails(current);
        when(userService.findUserById(2L)).thenReturn(target);
        when(followerService.isFollowing(current, target)).thenReturn(true);

        ResponseEntity<Boolean> response = followerController.isFollowing(userDetails, 2L);

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
