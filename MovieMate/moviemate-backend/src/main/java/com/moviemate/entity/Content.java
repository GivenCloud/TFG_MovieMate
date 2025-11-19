package com.moviemate.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "content")
public class Content {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Integer tmdbId;

    @Column(nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ContentType contentType;

    private LocalDate releaseDate;

    private String posterUrl;

    private String backdropUrl;

    @Column(columnDefinition = "TEXT")
    private String synopsis;

    private String[] genres;

    private Double averageRating = 0.0;

    private Integer voteCount = 0;

    @CreationTimestamp
    private LocalDateTime lastSync;

    public enum ContentType {
        MOVIE, TV
    }
}