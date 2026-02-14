package com.moviemate.service;

import com.moviemate.dto.UserResponse;
import com.moviemate.entity.FollowRequest;
import com.moviemate.entity.Follower;
import com.moviemate.entity.User;
import com.moviemate.repository.FollowRequestRepository;
import com.moviemate.repository.FollowerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FollowerServiceTest {

    private FollowerRepository followerRepository;
    private FollowRequestRepository followRequestRepository;
    private UserService userService;
    private FollowerService followerService;

    @BeforeEach
    void setUp() {
        followerRepository = mock(FollowerRepository.class);
        followRequestRepository = mock(FollowRequestRepository.class);
        userService = mock(UserService.class);
        followerService = new FollowerService(followerRepository, followRequestRepository, userService);
    }

    // ---------- unfollowUser ----------

    @Test
    void unfollowUser_shouldDeleteFollowerRelation_whenExists() {
        User follower = buildUser(1L, "chris", true);
        User followed = buildUser(2L, "alex", true);

        Follower followerEntity = buildFollower(1L, follower, followed);
        when(followerRepository.findByFollowerAndFollowed(follower, followed))
                .thenReturn(Optional.of(followerEntity));

        FollowRequest acceptedRequest = buildFollowRequest(10L, follower, followed, FollowRequest.FollowRequestStatus.ACCEPTED);
        when(followRequestRepository.findBySenderAndReceiverAndStatus(
                follower, followed, FollowRequest.FollowRequestStatus.ACCEPTED))
                .thenReturn(Optional.of(acceptedRequest));

        followerService.unfollowUser(follower, followed);

        verify(followerRepository).delete(followerEntity);
        assertThat(acceptedRequest.getStatus()).isEqualTo(FollowRequest.FollowRequestStatus.CANCELLED);
    }

    @Test
    void unfollowUser_shouldThrow_whenFollowerRelationNotFound() {
        User follower = buildUser(1L, "chris", true);
        User followed = buildUser(2L, "alex", true);

        when(followerRepository.findByFollowerAndFollowed(follower, followed))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> followerService.unfollowUser(follower, followed))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No estás siguiendo a este usuario");

        verify(followerRepository, never()).delete(any());
    }

    @Test
    void unfollowUser_shouldDeleteFollower_whenNoFollowRequestExists() {
        User follower = buildUser(1L, "chris", true);
        User followed = buildUser(2L, "alex", true);

        Follower followerEntity = buildFollower(1L, follower, followed);
        when(followerRepository.findByFollowerAndFollowed(follower, followed))
                .thenReturn(Optional.of(followerEntity));

        when(followRequestRepository.findBySenderAndReceiverAndStatus(
                follower, followed, FollowRequest.FollowRequestStatus.ACCEPTED))
                .thenReturn(Optional.empty());

        followerService.unfollowUser(follower, followed);

        verify(followerRepository).delete(followerEntity);
        verify(followRequestRepository).findBySenderAndReceiverAndStatus(
                follower, followed, FollowRequest.FollowRequestStatus.ACCEPTED);
    }

    // ---------- getFollowers ----------

    @Test
    void getFollowers_shouldReturnListOfFollowers_whenUserHasFollowers() {
        User user = buildUser(1L, "chris", true);
        User follower1 = buildUser(2L, "alex", true);
        User follower2 = buildUser(3L, "sam", true);

        Follower f1 = buildFollower(1L, follower1, user);
        Follower f2 = buildFollower(2L, follower2, user);

        when(userService.findUserById(1L)).thenReturn(user);
        when(followerRepository.findByFollowed(user)).thenReturn(List.of(f1, f2));

        UserResponse response1 = buildUserResponse(2L, "alex");
        UserResponse response2 = buildUserResponse(3L, "sam");

        when(userService.mapToUserResponse(follower1)).thenReturn(response1);
        when(userService.mapToUserResponse(follower2)).thenReturn(response2);

        List<UserResponse> followers = followerService.getFollowers(1L);

        assertThat(followers).hasSize(2);
        assertThat(followers)
                .extracting(UserResponse::getUsername)
                .containsExactlyInAnyOrder("alex", "sam");

        verify(userService).findUserById(1L);
        verify(followerRepository).findByFollowed(user);
    }

    @Test
    void getFollowers_shouldReturnEmptyList_whenUserHasNoFollowers() {
        User user = buildUser(1L, "chris", true);

        when(userService.findUserById(1L)).thenReturn(user);
        when(followerRepository.findByFollowed(user)).thenReturn(List.of());

        List<UserResponse> followers = followerService.getFollowers(1L);

        assertThat(followers).isEmpty();
        verify(followerRepository).findByFollowed(user);
    }

    @Test
    void getFollowers_shouldThrow_whenUserNotFound() {
        when(userService.findUserById(999L))
                .thenThrow(new RuntimeException("Usuario no encontrado"));

        assertThatThrownBy(() -> followerService.getFollowers(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");

        verify(followerRepository, never()).findByFollowed(any());
    }

    // ---------- getFollowing ----------

    @Test
    void getFollowing_shouldReturnListOfFollowedUsers_whenUserFollowsOthers() {
        User user = buildUser(1L, "chris", true);
        User followed1 = buildUser(2L, "alex", true);
        User followed2 = buildUser(3L, "sam", true);

        Follower f1 = buildFollower(1L, user, followed1);
        Follower f2 = buildFollower(2L, user, followed2);

        when(userService.findUserById(1L)).thenReturn(user);
        when(followerRepository.findByFollower(user)).thenReturn(List.of(f1, f2));

        UserResponse response1 = buildUserResponse(2L, "alex");
        UserResponse response2 = buildUserResponse(3L, "sam");

        when(userService.mapToUserResponse(followed1)).thenReturn(response1);
        when(userService.mapToUserResponse(followed2)).thenReturn(response2);

        List<UserResponse> following = followerService.getFollowing(1L);

        assertThat(following).hasSize(2);
        assertThat(following)
                .extracting(UserResponse::getUsername)
                .containsExactlyInAnyOrder("alex", "sam");

        verify(userService).findUserById(1L);
        verify(followerRepository).findByFollower(user);
    }

    @Test
    void getFollowing_shouldReturnEmptyList_whenUserFollowsNoOne() {
        User user = buildUser(1L, "chris", true);

        when(userService.findUserById(1L)).thenReturn(user);
        when(followerRepository.findByFollower(user)).thenReturn(List.of());

        List<UserResponse> following = followerService.getFollowing(1L);

        assertThat(following).isEmpty();
        verify(followerRepository).findByFollower(user);
    }

    @Test
    void getFollowing_shouldThrow_whenUserNotFound() {
        when(userService.findUserById(999L))
                .thenThrow(new RuntimeException("Usuario no encontrado"));

        assertThatThrownBy(() -> followerService.getFollowing(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");

        verify(followerRepository, never()).findByFollower(any());
    }

    // ---------- isFollowing ----------

    @Test
    void isFollowing_shouldReturnTrue_whenFollowerRelationExists() {
        User follower = buildUser(1L, "chris", true);
        User followed = buildUser(2L, "alex", true);

        when(followerRepository.existsByFollowerAndFollowed(follower, followed))
                .thenReturn(true);

        boolean result = followerService.isFollowing(follower, followed);

        assertThat(result).isTrue();
        verify(followerRepository).existsByFollowerAndFollowed(follower, followed);
    }

    @Test
    void isFollowing_shouldReturnFalse_whenFollowerRelationDoesNotExist() {
        User follower = buildUser(1L, "chris", true);
        User followed = buildUser(2L, "alex", true);

        when(followerRepository.existsByFollowerAndFollowed(follower, followed))
                .thenReturn(false);

        boolean result = followerService.isFollowing(follower, followed);

        assertThat(result).isFalse();
        verify(followerRepository).existsByFollowerAndFollowed(follower, followed);
    }

    // ---------- getFollowersCount ----------

    @Test
    void getFollowersCount_shouldReturnCorrectCount_whenUserHasFollowers() {
        User user = buildUser(1L, "chris", true);

        when(followerRepository.countFollowersByUserId(1L)).thenReturn(5);

        Integer count = followerService.getFollowersCount(user);

        assertThat(count).isEqualTo(5);
        verify(followerRepository).countFollowersByUserId(1L);
    }

    @Test
    void getFollowersCount_shouldReturnZero_whenUserHasNoFollowers() {
        User user = buildUser(1L, "chris", true);

        when(followerRepository.countFollowersByUserId(1L)).thenReturn(0);

        Integer count = followerService.getFollowersCount(user);

        assertThat(count).isEqualTo(0);
        verify(followerRepository).countFollowersByUserId(1L);
    }

    @Test
    void getFollowersCount_shouldHandleNull_whenRepositoryReturnsNull() {
        User user = buildUser(1L, "chris", true);

        when(followerRepository.countFollowersByUserId(1L)).thenReturn(null);

        Integer count = followerService.getFollowersCount(user);

        assertThat(count).isNull();
        verify(followerRepository).countFollowersByUserId(1L);
    }

    // ---------- getFollowingCount ----------

    @Test
    void getFollowingCount_shouldReturnCorrectCount_whenUserFollowsOthers() {
        User user = buildUser(1L, "chris", true);

        when(followerRepository.countFollowingByUserId(1L)).thenReturn(3);

        Integer count = followerService.getFollowingCount(user);

        assertThat(count).isEqualTo(3);
        verify(followerRepository).countFollowingByUserId(1L);
    }

    @Test
    void getFollowingCount_shouldReturnZero_whenUserFollowsNoOne() {
        User user = buildUser(1L, "chris", true);

        when(followerRepository.countFollowingByUserId(1L)).thenReturn(0);

        Integer count = followerService.getFollowingCount(user);

        assertThat(count).isEqualTo(0);
        verify(followerRepository).countFollowingByUserId(1L);
    }

    @Test
    void getFollowingCount_shouldHandleNull_whenRepositoryReturnsNull() {
        User user = buildUser(1L, "chris", true);

        when(followerRepository.countFollowingByUserId(1L)).thenReturn(null);

        Integer count = followerService.getFollowingCount(user);

        assertThat(count).isNull();
        verify(followerRepository).countFollowingByUserId(1L);
    }

    // ---------- helpers ----------

    private User buildUser(Long id, String username, boolean isPublic) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setEmail(username + "@example.com");
        u.setAvatarUrl("avatar_" + username + ".png");
        u.setIsPublic(isPublic);
        return u;
    }

    private Follower buildFollower(Long id, User follower, User followed) {
        Follower f = new Follower();
        f.setId(id);
        f.setFollower(follower);
        f.setFollowed(followed);
        return f;
    }

    private FollowRequest buildFollowRequest(Long id, User sender, User receiver, FollowRequest.FollowRequestStatus status) {
        FollowRequest fr = new FollowRequest();
        fr.setId(id);
        fr.setSender(sender);
        fr.setReceiver(receiver);
        fr.setStatus(status);
        return fr;
    }

    private UserResponse buildUserResponse(Long id, String username) {
        return UserResponse.builder()
                .id(id)
                .username(username)
                .email(username + "@example.com")
                .avatarUrl("avatar_" + username + ".png")
                .build();
    }
}