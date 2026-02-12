package com.moviemate.repository;

import com.moviemate.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ContentRepository extends JpaRepository<Content, Long> {
    Optional<Content> findByTmdbId(Integer tmdbId);
    Boolean existsByTmdbId(Integer tmdbId);

    @Modifying
    @Query("""
    delete from Content c
    where c.appVoteCount = 0
    and c.lastInteraction < :threshold
    """)
    void deleteUnused(LocalDateTime threshold);

}