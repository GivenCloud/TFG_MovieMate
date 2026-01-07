package com.moviemate.controller;

import com.moviemate.dto.RegisterRequest;
import com.moviemate.dto.AuthResponse;
import com.moviemate.dto.LoginRequest;
import com.moviemate.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// --- Swagger imports ---
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @Operation(
            summary = "Registrar un nuevo usuario",
            description = "Crea una cuenta nueva en la plataforma.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "Ejemplo registro válido",
                                            value = """
                                            {
                                              "username": "ejemploUsuario",
                                              "email": "correo@ejemplo.com",
                                              "password": "SuperSecreta123"
                                            }
                                            """
                                    )
                            }
                    )
            )
    )
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @org.springframework.web.bind.annotation.RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }
    
    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica un usuario con email o nombre de usuario y contraseña.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "Ejemplo login con email",
                                            value = """
                                            {
                                              "usernameOrEmail": "correo@ejemplo.com",
                                              "password": "SuperSecreta123"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Ejemplo login con username",
                                            value = """
                                            {
                                              "usernameOrEmail": "ejemploUsuario",
                                              "password": "SuperSecreta123"
                                            }
                                            """
                                    )
                            }
                    )
            )
    )
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @org.springframework.web.bind.annotation.RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }
}
