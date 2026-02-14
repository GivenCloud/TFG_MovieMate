package com.moviemate.service;

import com.moviemate.dto.AuthResponse;
import com.moviemate.dto.LoginRequest;
import com.moviemate.dto.RegisterRequest;
import com.moviemate.entity.User;
import com.moviemate.exception.UserAlreadyExistsException;
import com.moviemate.repository.UserRepository;
import com.moviemate.security.CustomUserDetails;
import com.moviemate.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthenticationManager authenticationManager;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        authenticationManager = mock(AuthenticationManager.class);
        
        authService = new AuthService(
            userRepository,
            passwordEncoder,
            jwtService,
            authenticationManager
        );
    }

    // ---------- register ----------

    @Test
    void register_shouldCreateUser_whenValidRequest() {
        RegisterRequest request = buildRegisterRequest("chris", "chris@example.com", "password123");

        when(userRepository.existsByUsername("chris")).thenReturn(false);
        when(userRepository.existsByEmail("chris@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("chris");
        savedUser.setEmail("chris@example.com");
        savedUser.setPasswordHash("encodedPassword");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(CustomUserDetails.class))).thenReturn("jwt-token-123");

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token-123");
        assertThat(response.getUsername()).isEqualTo("chris");
        assertThat(response.getEmail()).isEqualTo("chris@example.com");
        assertThat(response.getMessage()).isEqualTo("Usuario registrado exitosamente");

        verify(userRepository).existsByUsername("chris");
        verify(userRepository).existsByEmail("chris@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(CustomUserDetails.class));
    }

    @Test
    void register_shouldThrow_whenUsernameExists() {
        RegisterRequest request = buildRegisterRequest("chris", "chris@example.com", "password123");

        when(userRepository.existsByUsername("chris")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("El nombre de usuario ya existe");

        verify(userRepository).existsByUsername("chris");
        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldThrow_whenEmailExists() {
        RegisterRequest request = buildRegisterRequest("chris", "chris@example.com", "password123");

        when(userRepository.existsByUsername("chris")).thenReturn(false);
        when(userRepository.existsByEmail("chris@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("El email ya está registrado");

        verify(userRepository).existsByUsername("chris");
        verify(userRepository).existsByEmail("chris@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldEncodePassword() {
        RegisterRequest request = buildRegisterRequest("chris", "chris@example.com", "plainPassword");

        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode("plainPassword")).thenReturn("$2a$10$encodedHash");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("chris");
        savedUser.setEmail("chris@example.com");
        savedUser.setPasswordHash("$2a$10$encodedHash");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any())).thenReturn("token");

        authService.register(request);

        verify(passwordEncoder).encode("plainPassword");
        verify(userRepository).save(argThat(user -> 
            user.getPasswordHash().equals("$2a$10$encodedHash")
        ));
    }

    @Test
    void register_shouldGenerateJwtToken() {
        RegisterRequest request = buildRegisterRequest("chris", "chris@example.com", "password123");

        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("chris");
        savedUser.setEmail("chris@example.com");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(CustomUserDetails.class)))
                .thenReturn("generated-jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("generated-jwt-token");
        verify(jwtService).generateToken(any(CustomUserDetails.class));
    }

    // ---------- authenticate ----------

    @Test
    void authenticate_shouldReturnToken_whenCredentialsAreValid() {
        LoginRequest request = buildLoginRequest("chris", "password123");

        User user = new User();
        user.setId(1L);
        user.setUsername("chris");
        user.setEmail("chris@example.com");
        user.setPasswordHash("encodedPassword");

        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = mock(Authentication.class);
        
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token-456");

        AuthResponse response = authService.authenticate(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token-456");
        assertThat(response.getUsername()).isEqualTo("chris");
        assertThat(response.getEmail()).isEqualTo("chris@example.com");
        assertThat(response.getMessage()).isEqualTo("Login exitoso");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(userDetails);
    }

    @Test
    void authenticate_shouldThrow_whenCredentialsAreInvalid() {
        LoginRequest request = buildLoginRequest("chris", "wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThatThrownBy(() -> authService.authenticate(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Credenciales inválidas");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void authenticate_shouldAcceptEmail_asUsernameOrEmail() {
        LoginRequest request = buildLoginRequest("chris@example.com", "password123");

        User user = new User();
        user.setId(1L);
        user.setUsername("chris");
        user.setEmail("chris@example.com");

        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = mock(Authentication.class);
        
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(any())).thenReturn("token");

        AuthResponse response = authService.authenticate(request);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("chris@example.com");

        verify(authenticationManager).authenticate(argThat(token ->
            token.getPrincipal().equals("chris@example.com") &&
            token.getCredentials().equals("password123")
        ));
    }

    @Test
    void authenticate_shouldCallAuthenticationManager_withCorrectCredentials() {
        LoginRequest request = buildLoginRequest("testuser", "testpass");

        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(any())).thenReturn("token");

        authService.authenticate(request);

        verify(authenticationManager).authenticate(argThat(token ->
            token instanceof UsernamePasswordAuthenticationToken &&
            token.getPrincipal().equals("testuser") &&
            token.getCredentials().equals("testpass")
        ));
    }

    @Test
    void authenticate_shouldGenerateToken_fromAuthenticatedUser() {
        LoginRequest request = buildLoginRequest("chris", "password123");

        User user = new User();
        user.setId(1L);
        user.setUsername("chris");
        user.setEmail("chris@example.com");

        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateToken(userDetails)).thenReturn("specific-token");

        AuthResponse response = authService.authenticate(request);

        assertThat(response.getToken()).isEqualTo("specific-token");
        verify(jwtService).generateToken(userDetails);
    }

    @Test
    void authenticate_shouldThrow_whenAuthenticationManagerThrowsException() {
        LoginRequest request = buildLoginRequest("chris", "password123");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Authentication failed"));

        assertThatThrownBy(() -> authService.authenticate(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Credenciales inválidas");
    }

    // ---------- helpers ----------

    private RegisterRequest buildRegisterRequest(String username, String email, String password) {
        RegisterRequest req = new RegisterRequest();
        req.setUsername(username);
        req.setEmail(email);
        req.setPassword(password);
        return req;
    }

    private LoginRequest buildLoginRequest(String usernameOrEmail, String password) {
        LoginRequest req = new LoginRequest();
        req.setUsernameOrEmail(usernameOrEmail);
        req.setPassword(password);
        return req;
    }
}