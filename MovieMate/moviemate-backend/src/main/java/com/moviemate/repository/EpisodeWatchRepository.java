package com.moviemate.repository;

import com.moviemate.entity.EpisodeWatch;
import com.moviemate.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EpisodeWatchRepository extends JpaRepository<EpisodeWatch, Long> {

    List<EpisodeWatch> findByUserAndTmdbSeriesId(User user, Integer tmdbSeriesId);

    Optional<EpisodeWatch> findByUserAndTmdbSeriesIdAndSeasonNumberAndEpisodeNumber(
        User user, Integer tmdbSeriesId, Integer seasonNumber, Integer episodeNumber
    );

    boolean existsByUserAndTmdbSeriesIdAndSeasonNumberAndEpisodeNumber(
        User user, Integer tmdbSeriesId, Integer seasonNumber, Integer episodeNumber
    );

    @Query("SELECT COUNT(e) FROM EpisodeWatch e WHERE e.user = :user AND e.tmdbSeriesId = :seriesId AND e.seasonNumber = :season")
    int countByUserAndTmdbSeriesIdAndSeasonNumber(
        @Param("user") User user,
        @Param("seriesId") Integer seriesId,
        @Param("season") Integer season
    );
}
