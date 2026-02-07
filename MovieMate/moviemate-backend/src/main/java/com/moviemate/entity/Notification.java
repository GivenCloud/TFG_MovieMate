package com.moviemate.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "notifications")
@Data
public class Notification {
    @Id @GeneratedValue private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;  // Receptor
    
    @Column(nullable = false)
    private String type;  // "FOLLOW_REQUEST", "FOLLOW_APPROVED"
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    private boolean read = false;
    
    @CreationTimestamp private LocalDateTime createdAt;
    
    @ManyToOne
    private Follower relatedFollower;
}

