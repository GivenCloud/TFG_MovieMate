package com.moviemate.service;

import com.moviemate.dto.SeriesProgressDto;
import com.moviemate.entity.Content;
import com.moviemate.entity.EpisodeWatch;
import com.moviemate.entity.User;
import com.moviemate.repository.ContentRepository;
import com.moviemate.repository.EpisodeWatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EpisodeWatchService {

    private final EpisodeWatchRepository episodeWatchRepository;
    private final ContentRepository contentRepository;

    /**
     * Devuelve el conjunto de episodios vistos por el usuario para una serie,
     * como strings "seasonNumber-episodeNumber" (p.ej. "1-3").
     */
    public Set<String> getWatchedEpisodes(User user, Integer tmdbSeriesId) {
        return episodeWatchRepository.findByUserAndTmdbSeriesId(user, tmdbSeriesId)
                .stream()
                .map(e -> e.getSeasonNumber() + "-" + e.getEpisodeNumber())
                .collect(Collectors.toSet());
    }

    /**
     * Marca o desmarca un episodio como visto.
     * Devuelve true si ahora está marcado, false si se desmarcó.
     */
    @Transactional
    public boolean toggleEpisodeWatched(User user, Integer tmdbSeriesId,
                                        Integer seasonNumber, Integer episodeNumber) {
        Optional<EpisodeWatch> existing = episodeWatchRepository
                .findByUserAndTmdbSeriesIdAndSeasonNumberAndEpisodeNumber(
                        user, tmdbSeriesId, seasonNumber, episodeNumber);

        if (existing.isPresent()) {
            episodeWatchRepository.delete(existing.get());
            return false;
        } else {
            EpisodeWatch watch = new EpisodeWatch();
            watch.setUser(user);
            watch.setTmdbSeriesId(tmdbSeriesId);
            watch.setSeasonNumber(seasonNumber);
            watch.setEpisodeNumber(episodeNumber);
            episodeWatchRepository.save(watch);
            return true;
        }
    }

    /**
     * Marca todos los episodios de una temporada como vistos.
     * episodeNumbers: lista de todos los números de episodio de esa temporada.
     */
    @Transactional
    public void markSeasonWatched(User user, Integer tmdbSeriesId,
                                  Integer seasonNumber, List<Integer> episodeNumbers) {
        for (Integer epNum : episodeNumbers) {
            boolean exists = episodeWatchRepository
                    .existsByUserAndTmdbSeriesIdAndSeasonNumberAndEpisodeNumber(
                            user, tmdbSeriesId, seasonNumber, epNum);
            if (!exists) {
                EpisodeWatch watch = new EpisodeWatch();
                watch.setUser(user);
                watch.setTmdbSeriesId(tmdbSeriesId);
                watch.setSeasonNumber(seasonNumber);
                watch.setEpisodeNumber(epNum);
                episodeWatchRepository.save(watch);
            }
        }
    }

    /**
     * Desmarca todos los episodios de una temporada.
     */
    @Transactional
    public void unmarkSeasonWatched(User user, Integer tmdbSeriesId, Integer seasonNumber) {
        List<EpisodeWatch> entries = episodeWatchRepository
                .findByUserAndTmdbSeriesId(user, tmdbSeriesId)
                .stream()
                .filter(e -> e.getSeasonNumber().equals(seasonNumber))
                .collect(Collectors.toList());
        episodeWatchRepository.deleteAll(entries);
    }

    /**
     * Devuelve el progreso de episodios vistos del usuario agrupado por serie,
     * enriquecido con el título y poster de la serie si existe en la base de datos local.
     */
    public List<SeriesProgressDto> getSeriesProgress(User user) {
        return episodeWatchRepository.findWatchedCountByUserGroupedBySeries(user)
                .stream()
                .map(row -> {
                    Integer tmdbId = (Integer) row[0];
                    long count = (Long) row[1];
                    Optional<Content> content = contentRepository.findByTmdbId(tmdbId);
                    String title = content.map(Content::getTitle).orElse("Serie " + tmdbId);
                    String poster = content.map(Content::getPosterUrl).orElse(null);
                    return new SeriesProgressDto(tmdbId, title, poster, count);
                })
                .collect(Collectors.toList());
    }
}
