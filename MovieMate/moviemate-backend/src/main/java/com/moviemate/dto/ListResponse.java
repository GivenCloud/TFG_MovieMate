package com.moviemate.dto;

import com.moviemate.entity.List;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ListResponse {
    private Long id;
    private String name;
    private String description;
    private Boolean isPublic;
    private List.ListType listType;
    private Integer itemCount;
    private LocalDateTime createdAt;
    private UserResponse user;
    private java.util.List<ContentSimpleResponse> contents;
}