package com.moviemate.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "content")
public class Content {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Integer tmdbId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentType contentType;

    // ------------ TMDB ------------

    @Column(nullable = false)
    private String title;
    private LocalDate releaseDate;

    @Column(columnDefinition = "TEXT")
    private String synopsis;

    private String posterUrl;
    private String backdropUrl;

    @ElementCollection 
    @CollectionTable(name = "content_genres", joinColumns = @JoinColumn(name = "content_id")) @Column(name = "genre")
    private List<String> genres;

    private Double tmdbRating = 0.0;
    private Integer tmdbVoteCount = 0;

    // ------------ APP ------------ 
    private Double appRating = 0.0; 
    private Integer appVoteCount = 0;

    private LocalDateTime lastTmdbSync;
    private LocalDateTime lastInteraction;

    @Enumerated(EnumType.STRING) 
    private SyncStatus syncStatus = SyncStatus.FRESH;

    public enum ContentType {
        MOVIE, 
        TV
    }

    public enum SyncStatus {
        FRESH,
        STALE,
        UPDATING
    }
}