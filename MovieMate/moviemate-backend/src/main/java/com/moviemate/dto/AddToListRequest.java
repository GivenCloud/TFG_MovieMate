package com.moviemate.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddToListRequest {
    @NotBlank
    private Integer tmdbId;
}
