package com.moviemate.security;

import com.moviemate.entity.User;
import com.moviemate.exception.ProfilePrivateException;
import com.moviemate.repository.FollowerRepository;
import com.moviemate.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ProfileSecurityServiceTest {

    private UserRepository userRepository;
    private FollowerRepository followerRepository;
    private ProfileSecurityService profileSecurityService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        followerRepository = mock(FollowerRepository.class);
        profileSecurityService = new ProfileSecurityService();
        ReflectionTestUtils.setField(profileSecurityService, "userRepository", userRepository);
        ReflectionTestUtils.setField(profileSecurityService, "followerRepository", followerRepository);
    }

    @Test
    void canViewProfile_shouldAllowOwnProfile() {
        User current = user(1L, "ana", false);
        User target = user(1L, "ana", false);
        when(userRepository.findByUsername("ana")).thenReturn(Optional.of(current));
        when(userRepository.findById(1L)).thenReturn(Optional.of(target));

        boolean result = profileSecurityService.canViewProfile("ana", 1L);

        assertThat(result).isTrue();
    }

    @Test
    void canViewProfile_shouldAllowPublicProfile() {
        User target = user(2L, "bob", true);
        when(userRepository.findByUsername("ana")).thenReturn(Optional.empty());
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        boolean result = profileSecurityService.canViewProfile("ana", 2L);

        assertThat(result).isTrue();
    }

    @Test
    void canViewProfile_shouldAllowFollower() {
        User current = user(1L, "ana", false);
        User target = user(2L, "bob", false);
        when(userRepository.findByUsername("ana")).thenReturn(Optional.of(current));
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(followerRepository.existsByFollowerAndFollowed(current, target)).thenReturn(true);

        boolean result = profileSecurityService.canViewProfile("ana", 2L);

        assertThat(result).isTrue();
    }

    @Test
    void canViewProfile_shouldThrowWhenPrivateAndNotFollower() {
        User current = user(1L, "ana", false);
        User target = user(2L, "bob", false);
        when(userRepository.findByUsername("ana")).thenReturn(Optional.of(current));
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(followerRepository.existsByFollowerAndFollowed(current, target)).thenReturn(false);

        assertThatThrownBy(() -> profileSecurityService.canViewProfile("ana", 2L))
                .isInstanceOf(ProfilePrivateException.class);
    }

    @Test
    void canViewProfile_shouldThrowWhenTargetNotFound() {
        when(userRepository.findByUsername("ana")).thenReturn(Optional.empty());
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileSecurityService.canViewProfile("ana", 99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    private User user(Long id, String username, boolean isPublic) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setIsPublic(isPublic);
        return u;
    }
}
