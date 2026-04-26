package com.moviemate.config;

import com.moviemate.entity.User;
import com.moviemate.repository.UserRepository;
import com.moviemate.security.JwtAuthenticationFilter;
import com.moviemate.security.JwtService;
import com.moviemate.security.ProfileSecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    private UserRepository userRepository;
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        securityConfig = new SecurityConfig(userRepository);
        ReflectionTestUtils.setField(securityConfig, "corsAllowedOrigins", "http://localhost:5173,http://example.com");
    }

    @Test
    void userDetailsService_shouldLoadUserAndThrowWhenMissing() {
        User user = new User();
        user.setUsername("ana");
        user.setEmail("ana@mail.com");
        user.setPasswordHash("hash");
        when(userRepository.findByUsernameOrEmail("ana", "ana")).thenReturn(Optional.of(user));

        UserDetailsService uds = securityConfig.userDetailsService();
        assertThat(uds.loadUserByUsername("ana").getUsername()).isEqualTo("ana");

        when(userRepository.findByUsernameOrEmail("missing", "missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> uds.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void authenticationProvider_shouldCreateProvider() {
        AuthenticationProvider provider = securityConfig.authenticationProvider();
        assertThat(provider).isNotNull();
    }

    @Test
    void jwtAuthenticationFilter_shouldCreateBean() {
        JwtService jwtService = mock(JwtService.class);
        UserDetailsService uds = mock(UserDetailsService.class);
        JwtAuthenticationFilter filter = securityConfig.jwtAuthenticationFilter(jwtService, uds);

        assertThat(filter).isNotNull();
    }

    @Test
    void corsConfigurationSource_shouldContainConfiguredOrigins() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration cors = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/test"));

        assertThat(cors).isNotNull();
        assertThat(cors.getAllowedOrigins()).contains("http://localhost:5173", "http://example.com");
        assertThat(cors.getAllowedMethods()).contains("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    }

    @Test
    void passwordEncoder_shouldEncodeAndMatch() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String encoded = encoder.encode("secret");

        assertThat(encoder.matches("secret", encoded)).isTrue();
    }

    @Test
    void authenticationManager_shouldReturnConfiguredManager() throws Exception {
        AuthenticationConfiguration config = mock(AuthenticationConfiguration.class);
        AuthenticationManager manager = mock(AuthenticationManager.class);
        when(config.getAuthenticationManager()).thenReturn(manager);

        AuthenticationManager result = securityConfig.authenticationManager(config);
        assertThat(result).isSameAs(manager);
    }

    @Test
    void profileSecurityService_shouldCreateBean() {
        ProfileSecurityService pss = securityConfig.profileSecurityService();
        assertThat(pss).isNotNull();
    }
}
