package com.moviemate.dto;

import com.moviemate.entity.List;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ListRequest {
    @NotBlank(message = "El nombre de la lista es obligatorio")
    private String name;
    
    private String description;
    private Boolean isPublic = true;
    private List.ListType listType = List.ListType.CUSTOM;
}