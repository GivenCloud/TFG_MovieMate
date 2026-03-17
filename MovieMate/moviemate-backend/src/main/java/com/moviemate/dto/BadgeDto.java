package com.moviemate.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BadgeDto {
    private String type;
    private String name;
    private String description;
    private String icon;
    private LocalDateTime awardedAt;
}
