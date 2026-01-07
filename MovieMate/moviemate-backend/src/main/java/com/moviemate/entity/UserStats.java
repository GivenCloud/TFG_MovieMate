package com.moviemate.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_stats")
public class UserStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private Integer totalRatings = 0;
    private Double averageRating = 0.0;
    private Integer moviesWatched = 0;
    private Integer seriesWatched = 0;
    private Integer totalWatchTime = 0; // en minutos

    private Integer listsCreated = 0;
    private Integer followersCount = 0;
    private Integer followingCount = 0;
    private Integer likesReceived = 0;


    @UpdateTimestamp
    private LocalDateTime lastActivity;
}