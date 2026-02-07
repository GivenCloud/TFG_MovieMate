package com.moviemate.dto;

import java.time.LocalDateTime;

import com.moviemate.entity.FollowRequest;

import lombok.Data;

@Data
public class FollowRequestDto {
    private Long id;
    private Long senderId;
    private String senderUsername;
    private String senderAvatarUrl;
    private LocalDateTime createdAt;

    public static FollowRequestDto from(FollowRequest request) {
        FollowRequestDto dto = new FollowRequestDto();
        dto.setId(request.getId());
        dto.setSenderId(request.getSender().getId());
        dto.setSenderUsername(request.getSender().getUsername());
        dto.setSenderAvatarUrl(request.getSender().getAvatarUrl());
        dto.setCreatedAt(request.getCreatedAt());
        return dto;
    }
}