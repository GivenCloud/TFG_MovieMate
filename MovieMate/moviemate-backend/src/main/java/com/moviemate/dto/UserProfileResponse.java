package com.moviemate.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserProfileResponse extends UserResponse {
    private Integer followersCount;
    private Integer followingCount;
    private Boolean isFollowing; // Si el usuario autenticado sigue a este usuario
}