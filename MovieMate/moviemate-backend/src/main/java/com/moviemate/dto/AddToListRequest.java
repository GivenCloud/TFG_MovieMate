package com.moviemate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddToListRequest {
    @NotNull
    private Integer tmdbId;
}
