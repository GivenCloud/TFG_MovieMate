package com.moviemate.service;

import com.moviemate.entity.EpisodeWatch;
import com.moviemate.entity.User;
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
}
