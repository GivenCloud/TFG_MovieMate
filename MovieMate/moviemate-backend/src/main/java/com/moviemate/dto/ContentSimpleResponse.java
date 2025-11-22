package com.moviemate.dto;

import com.moviemate.entity.Content;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContentSimpleResponse {
    private Long id;
    private String title;
    private String posterUrl;
    private Content.ContentType contentType;
}