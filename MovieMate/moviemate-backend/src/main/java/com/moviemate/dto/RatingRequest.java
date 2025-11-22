package com.moviemate.dto;

import com.moviemate.entity.Rating;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RatingRequest {
    @NotNull(message = "El ID del contenido es obligatorio")
    private Long contentId;
    
    @Min(value = 1, message = "La puntuación debe ser al menos 1")
    @Max(value = 5, message = "La puntuación debe ser como máximo 5")
    private Integer rating;
    
    private String reviewText;
    private Rating.EmotionalTag emotionalTag;
    private Rating.Status status;

    @PastOrPresent(message = "La fecha de visualización no puede ser en el futuro")
    private LocalDate watchedDate;
}