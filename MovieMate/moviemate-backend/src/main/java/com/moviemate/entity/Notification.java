package com.moviemate.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Usuario que recibe la notificación
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    // ID del recurso relacionado (FollowRequest, Post, etc.)
    @Column(nullable = false)
    private Long referenceId;

    @Column(nullable = false)
    private boolean read = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum NotificationType {
        FOLLOW_REQUEST,
        FOLLOW_REQUEST_ACCEPTED,
        FOLLOWER,
        REVIEW_LIKE
    }
}
