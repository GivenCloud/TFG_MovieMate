package com.moviemate.security;

import com.moviemate.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "12345678901234567890123456789012");

        User user = new User();
        user.setUsername("ana");
        user.setPasswordHash("hash");
        userDetails = new CustomUserDetails(user);
    }

    @Test
    void generateAndExtract_shouldWork() {
        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("ana");
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void generateWithExtraClaims_shouldIncludeClaims() {
        String token = jwtService.generateToken(Map.of("scope", "admin"), userDetails);

        String scope = jwtService.extractClaim(token, claims -> claims.get("scope", String.class));
        assertThat(scope).isEqualTo("admin");
    }

    @Test
    void isTokenValid_shouldFailForDifferentUser() {
        String token = jwtService.generateToken(userDetails);

        User other = new User();
        other.setUsername("other");
        other.setPasswordHash("hash");
        UserDetails otherDetails = new CustomUserDetails(other);

        assertThat(jwtService.isTokenValid(token, otherDetails)).isFalse();
    }
}
