package com.moviemate.service;

import com.moviemate.dto.UserResponse;
import com.moviemate.entity.User;
import com.moviemate.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = new UserService(userRepository, passwordEncoder);
    }

    // ---------- findUserById ----------

    @Test
    void findUserById_shouldReturnUser_whenExists() {
        User user = buildUser(1L, "chris");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.findUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("chris");
        verify(userRepository).findById(1L);
    }

    @Test
    void findUserById_shouldThrow_whenNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");

        verify(userRepository).findById(999L);
    }

    // ---------- findUserByUsername ----------

    @Test
    void findUserByUsername_shouldReturnUser_whenExists() {
        User user = buildUser(1L, "chris");
        when(userRepository.findByUsername("chris")).thenReturn(Optional.of(user));

        User result = userService.findUserByUsername("chris");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("chris");
        verify(userRepository).findByUsername("chris");
    }

    @Test
    void findUserByUsername_shouldThrow_whenNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserByUsername("unknown"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");

        verify(userRepository).findByUsername("unknown");
    }

    // ---------- searchUsers ----------

    @Test
    void searchUsers_shouldReturnUsers_whenMatchesFound() {
        User user1 = buildUser(2L, "alex");
        User user2 = buildUser(3L, "alexander");

        when(userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase("alex", "alex"))
                .thenReturn(List.of(user1, user2));

        List<UserResponse> results = userService.searchUsers(1L, "alex");

        assertThat(results).hasSize(2);
        assertThat(results)
                .extracting(UserResponse::getUsername)
                .containsExactlyInAnyOrder("alex", "alexander");
        verify(userRepository).findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase("alex", "alex");
    }

    @Test
    void searchUsers_shouldExcludeCurrentUser() {
        User currentUser = buildUser(1L, "chris");
        User user1 = buildUser(2L, "christian");

        when(userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase("chris", "chris"))
                .thenReturn(List.of(currentUser, user1));

        List<UserResponse> results = userService.searchUsers(1L, "chris");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUsername()).isEqualTo("christian");
    }

    @Test
    void searchUsers_shouldReturnEmpty_whenNoMatches() {
        when(userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase("xyz", "xyz"))
                .thenReturn(List.of());

        List<UserResponse> results = userService.searchUsers(1L, "xyz");

        assertThat(results).isEmpty();
    }

    @Test
    void searchUsers_shouldSearchByEmail() {
        User user = buildUser(2L, "alex");
        user.setEmail("alex@example.com");

        when(userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase("example", "example"))
                .thenReturn(List.of(user));

        List<UserResponse> results = userService.searchUsers(1L, "example");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getEmail()).contains("example");
    }

    // ---------- getSuggestedUsers ----------

    @Test
    void getSuggestedUsers_shouldReturnRecentUsers_excludingCurrent() {
        User currentUser = buildUser(1L, "chris");
        User user1 = buildUser(2L, "alex");
        User user2 = buildUser(3L, "sam");
        User user3 = buildUser(4L, "jordan");

        when(userRepository.findTop10ByOrderByCreatedAtDesc())
                .thenReturn(List.of(user3, user2, user1, currentUser));

        List<UserResponse> results = userService.getSuggestedUsers(1L);

        assertThat(results).hasSize(3);
        assertThat(results)
                .extracting(UserResponse::getUsername)
                .containsExactly("jordan", "sam", "alex")
                .doesNotContain("chris");
        verify(userRepository).findTop10ByOrderByCreatedAtDesc();
    }

    @Test
    void getSuggestedUsers_shouldReturnEmpty_whenOnlyCurrentUser() {
        User currentUser = buildUser(1L, "chris");

        when(userRepository.findTop10ByOrderByCreatedAtDesc())
                .thenReturn(List.of(currentUser));

        List<UserResponse> results = userService.getSuggestedUsers(1L);

        assertThat(results).isEmpty();
    }

    @Test
    void getSuggestedUsers_shouldReturnUpTo10Users() {
        List<User> users = new java.util.ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            users.add(buildUser((long) i, "user" + i));
        }

        when(userRepository.findTop10ByOrderByCreatedAtDesc())
                .thenReturn(users.subList(0, 10));

        List<UserResponse> results = userService.getSuggestedUsers(99L);

        assertThat(results).hasSizeLessThanOrEqualTo(10);
    }

    // ---------- updateUserProfile ----------

    @Test
    void updateUserProfile_shouldUpdateBioAndAvatar() {
        User user = buildUser(1L, "chris");
        user.setBio("Old bio");
        user.setAvatarUrl("old-avatar.jpg");

        User updatedUser = buildUser(1L, "chris");
        updatedUser.setBio("New bio");
        updatedUser.setAvatarUrl("new-avatar.jpg");

        when(userRepository.save(user)).thenReturn(updatedUser);

        UserResponse response = userService.updateUserProfile(user, "New bio", "new-avatar.jpg");

        assertThat(user.getBio()).isEqualTo("New bio");
        assertThat(user.getAvatarUrl()).isEqualTo("new-avatar.jpg");
        assertThat(response.getBio()).isEqualTo("New bio");
        verify(userRepository).save(user);
    }

    @Test
    void updateUserProfile_shouldUpdateOnlyBio_whenAvatarIsNull() {
        User user = buildUser(1L, "chris");
        user.setBio("Old bio");
        user.setAvatarUrl("old-avatar.jpg");

        when(userRepository.save(user)).thenReturn(user);

        userService.updateUserProfile(user, "New bio", null);

        assertThat(user.getBio()).isEqualTo("New bio");
        assertThat(user.getAvatarUrl()).isEqualTo("old-avatar.jpg"); // No cambió
        verify(userRepository).save(user);
    }

    @Test
    void updateUserProfile_shouldUpdateOnlyBio_whenAvatarIsEmpty() {
        User user = buildUser(1L, "chris");
        user.setBio("Old bio");
        user.setAvatarUrl("old-avatar.jpg");

        when(userRepository.save(user)).thenReturn(user);

        userService.updateUserProfile(user, "New bio", "   ");

        assertThat(user.getBio()).isEqualTo("New bio");
        assertThat(user.getAvatarUrl()).isEqualTo("old-avatar.jpg"); // No cambió
    }

    @Test
    void updateUserProfile_shouldUpdateAvatar_whenValid() {
        User user = buildUser(1L, "chris");
        user.setAvatarUrl("old-avatar.jpg");

        when(userRepository.save(user)).thenReturn(user);

        userService.updateUserProfile(user, "Bio", "new-avatar.jpg");

        assertThat(user.getAvatarUrl()).isEqualTo("new-avatar.jpg");
    }

    // ---------- updateUserPublicStatus ----------

    @Test
    void updateUserPublicStatus_shouldSetPublicToTrue() {
        User user = buildUser(1L, "chris");
        user.setIsPublic(false);

        when(userRepository.save(user)).thenReturn(user);

        UserResponse response = userService.updateUserPublicStatus(user, true);

        assertThat(user.getIsPublic()).isTrue();
        assertThat(response.getIsPublic()).isTrue();
        verify(userRepository).save(user);
    }

    @Test
    void updateUserPublicStatus_shouldSetPublicToFalse() {
        User user = buildUser(1L, "chris");
        user.setIsPublic(true);

        when(userRepository.save(user)).thenReturn(user);

        UserResponse response = userService.updateUserPublicStatus(user, false);

        assertThat(user.getIsPublic()).isFalse();
        assertThat(response.getIsPublic()).isFalse();
        verify(userRepository).save(user);
    }

    // ---------- mapToUserResponse ----------

    @Test
    void mapToUserResponse_shouldMapAllFields() {
        User user = buildUser(1L, "chris");
        user.setEmail("chris@example.com");
        user.setAvatarUrl("avatar.jpg");
        user.setBio("My bio");
        user.setIsPublic(true);
        user.setCreatedAt(LocalDateTime.now());

        UserResponse response = userService.mapToUserResponse(user);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("chris");
        assertThat(response.getEmail()).isEqualTo("chris@example.com");
        assertThat(response.getAvatarUrl()).isEqualTo("avatar.jpg");
        assertThat(response.getBio()).isEqualTo("My bio");
        assertThat(response.getIsPublic()).isTrue();
        assertThat(response.getCreatedAt()).isNotNull();
    }

    @Test
    void mapToUserResponse_shouldUseDefaultValues_whenFieldsAreNull() {
        User user = new User();
        user.setId(1L);
        user.setUsername("chris");

        UserResponse response = userService.mapToUserResponse(user);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("chris");
        assertThat(response.getEmail()).isNull();
        // avatarUrl tiene valor por defecto "/images/default-avatar.png" en la entidad User
        assertThat(response.getAvatarUrl()).isEqualTo("/images/default-avatar.png");
        assertThat(response.getBio()).isNull();
    }

    // ---------- helpers ----------

    private User buildUser(Long id, String username) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setEmail(username + "@example.com");
        u.setAvatarUrl("avatar.jpg");
        u.setBio("Bio of " + username);
        u.setIsPublic(false);
        u.setCreatedAt(LocalDateTime.now());
        return u;
    }
}