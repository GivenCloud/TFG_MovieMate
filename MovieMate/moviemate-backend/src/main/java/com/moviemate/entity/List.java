package com.moviemate.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Data
@Entity
@Table(
    name = "lists",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_lists_user_name",
        columnNames = {"user_id", "name"}
    )
)
public class List {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, columnDefinition = "boolean not null default true")
    private Boolean isPublic = true;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ListType listType = ListType.CUSTOM;

    @OneToMany(mappedBy = "list", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<ListContent> contents = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum ListType {
        CUSTOM, FAVORITES, WATCHLIST, WATCHED
    }
}