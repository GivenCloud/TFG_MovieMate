package com.moviemate.dto;

import lombok.Data;

import java.time.LocalDateTime;

import com.moviemate.entity.Notification.NotificationType;

@Data
public class NotificationDto {

    private Long id;
    private NotificationType type;
    private Long referenceId;
    private boolean read;
    private LocalDateTime createdAt;

    // info útil para UI
    private Long senderId;
    private String senderUsername;
    private String senderAvatarUrl;

    // Texto libre para notificaciones del sistema
    private String message;
}
