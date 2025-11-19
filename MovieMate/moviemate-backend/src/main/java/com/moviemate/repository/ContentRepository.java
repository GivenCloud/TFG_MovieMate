package com.moviemate.repository;

import com.moviemate.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ContentRepository extends JpaRepository<Content, Long> {
    Optional<Content> findByTmdbId(Integer tmdbId);
    Boolean existsByTmdbId(Integer tmdbId);
}