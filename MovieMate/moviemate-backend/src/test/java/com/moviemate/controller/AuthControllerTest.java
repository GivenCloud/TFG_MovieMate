package com.moviemate.controller;

import com.moviemate.dto.AuthResponse;
import com.moviemate.dto.LoginRequest;
import com.moviemate.dto.RegisterRequest;
import com.moviemate.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private AuthService authService;
    private AuthController authController;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        authController = new AuthController(authService);
    }

    @Test
    void register_shouldReturnOkWithBody() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("chris");
        request.setEmail("chris@mail.com");
        request.setPassword("123456");

        AuthResponse expected = AuthResponse.builder()
                .token("token-1")
                .username("chris")
                .email("chris@mail.com")
                .message("ok")
                .build();

        when(authService.register(request)).thenReturn(expected);

        ResponseEntity<AuthResponse> response = authController.register(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
        verify(authService).register(request);
    }

    @Test
    void login_shouldReturnOkWithBody() {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("chris@mail.com");
        request.setPassword("123456");

        AuthResponse expected = AuthResponse.builder()
                .token("token-2")
                .username("chris")
                .email("chris@mail.com")
                .message("ok")
                .build();

        when(authService.authenticate(request)).thenReturn(expected);

        ResponseEntity<AuthResponse> response = authController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
        verify(authService).authenticate(request);
    }
}
