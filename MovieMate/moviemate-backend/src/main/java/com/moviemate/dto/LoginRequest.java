package com.moviemate.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "El nombre de usuario o email es obligatorio")
    private String usernameOrEmail;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}