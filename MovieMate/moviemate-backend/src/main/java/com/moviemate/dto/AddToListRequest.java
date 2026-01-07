package com.moviemate.dto;

import com.moviemate.entity.Content;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddToListRequest {
    @NotBlank
    private Integer tmdbId;
    
    @NotBlank
    private Content.ContentType contentType;
}
