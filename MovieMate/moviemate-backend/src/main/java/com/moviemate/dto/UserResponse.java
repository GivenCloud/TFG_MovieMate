package com.moviemate.dto;
import com.moviemate.entity.Role;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private String bio;
    private Boolean isPublic;
    private Role role;
    private Boolean banned;
    private LocalDateTime createdAt;
}