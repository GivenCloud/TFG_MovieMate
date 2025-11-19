package com.moviemate.service;

import com.moviemate.dto.RegisterRequest;
import com.moviemate.dto.AuthResponse;
import com.moviemate.dto.LoginRequest;
import com.moviemate.entity.User;
import com.moviemate.exception.UserAlreadyExistsException;
import com.moviemate.repository.UserRepository;
import com.moviemate.security.CustomUserDetails;
import com.moviemate.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Validar que no exista el usuario
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("El nombre de usuario ya existe");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("El email ya está registrado");
        }
        
        // Crear usuario
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        
        User savedUser = userRepository.save(user);
        
        // Generar token
        CustomUserDetails userDetails = new CustomUserDetails(savedUser);
        String token = jwtService.generateToken(userDetails);
        
        return AuthResponse.builder()
                .token(token)
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .message("Usuario registrado exitosamente")
                .build();
    }
    
    public AuthResponse authenticate(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsernameOrEmail(),
                    request.getPassword()
                )
            );
            
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            String token = jwtService.generateToken(userDetails);

            return AuthResponse.builder()
                    .token(token)
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .message("Login exitoso")
                    .build();
                    
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
    }
}