package com.moviemate.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String bio;
    private String avatarUrl;
}
