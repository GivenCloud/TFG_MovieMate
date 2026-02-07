package com.moviemate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class FollowRequestActionResponse {

    private Long requestId;
    private Long senderId;
    private Long receiverId;
    private FollowRequestStatus status;
    private LocalDateTime actionAt;

    public enum FollowRequestStatus {
        ACCEPTED,
        REJECTED
    }
}
