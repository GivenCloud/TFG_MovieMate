package com.moviemate.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FollowRequest {
    @NotBlank(message = "El ID del usuario es obligatorio")
    private Long userId; // ID del usuario a seguir
}