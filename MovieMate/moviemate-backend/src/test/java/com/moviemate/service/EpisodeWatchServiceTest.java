package com.moviemate.service;

import com.moviemate.dto.SeriesProgressDto;
import com.moviemate.entity.Content;
import com.moviemate.entity.EpisodeWatch;
import com.moviemate.entity.User;
import com.moviemate.repository.ContentRepository;
import com.moviemate.repository.EpisodeWatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class EpisodeWatchServiceTest {

    private EpisodeWatchRepository episodeWatchRepository;
    private ContentRepository contentRepository;
    private EpisodeWatchService episodeWatchService;

    @BeforeEach
    void setUp() {
        episodeWatchRepository = mock(EpisodeWatchRepository.class);
        contentRepository = mock(ContentRepository.class);
        episodeWatchService = new EpisodeWatchService(episodeWatchRepository, contentRepository);
    }

    // ---------- getWatchedEpisodes ----------

    @Test
    void getWatchedEpisodes_shouldReturnFormattedKeys() {
        User user = buildUser(1L);
        EpisodeWatch e1 = buildEpisode(user, 100, 1, 1);
        EpisodeWatch e2 = buildEpisode(user, 100, 1, 3);
        EpisodeWatch e3 = buildEpisode(user, 100, 2, 1);

        when(episodeWatchRepository.findByUserAndTmdbSeriesId(user, 100))
                .thenReturn(List.of(e1, e2, e3));

        Set<String> result = episodeWatchService.getWatchedEpisodes(user, 100);

        assertThat(result).containsExactlyInAnyOrder("1-1", "1-3", "2-1");
        verify(episodeWatchRepository).findByUserAndTmdbSeriesId(user, 100);
    }

    @Test
    void getWatchedEpisodes_shouldReturnEmpty_whenNoneWatched() {
        User user = buildUser(1L);
        when(episodeWatchRepository.findByUserAndTmdbSeriesId(user, 100)).thenReturn(List.of());

        Set<String> result = episodeWatchService.getWatchedEpisodes(user, 100);

        assertThat(result).isEmpty();
    }

    // ---------- toggleEpisodeWatched ----------

    @Test
    void toggleEpisodeWatched_shouldMarkAsWatched_whenNotYetWatched() {
        User user = buildUser(1L);

        when(episodeWatchRepository
                .findByUserAndTmdbSeriesIdAndSeasonNumberAndEpisodeNumber(user, 100, 1, 2))
                .thenReturn(Optional.empty());
        when(episodeWatchRepository.save(any(EpisodeWatch.class)))
                .thenAnswer(i -> i.getArgument(0));

        boolean result = episodeWatchService.toggleEpisodeWatched(user, 100, 1, 2);

        assertThat(result).isTrue();
        verify(episodeWatchRepository).save(argThat(e ->
                e.getTmdbSeriesId().equals(100) &&
                e.getSeasonNumber().equals(1) &&
                e.getEpisodeNumber().equals(2)
        ));
    }

    @Test
    void toggleEpisodeWatched_shouldUnmarkAsWatched_whenAlreadyWatched() {
        User user = buildUser(1L);
        EpisodeWatch existing = buildEpisode(user, 100, 1, 2);

        when(episodeWatchRepository
                .findByUserAndTmdbSeriesIdAndSeasonNumberAndEpisodeNumber(user, 100, 1, 2))
                .thenReturn(Optional.of(existing));

        boolean result = episodeWatchService.toggleEpisodeWatched(user, 100, 1, 2);

        assertThat(result).isFalse();
        verify(episodeWatchRepository).delete(existing);
        verify(episodeWatchRepository, never()).save(any());
    }

    // ---------- markSeasonWatched ----------

    @Test
    void markSeasonWatched_shouldSaveOnlyMissingEpisodes() {
        User user = buildUser(1L);

        when(episodeWatchRepository.existsByUserAndTmdbSeriesIdAndSeasonNumberAndEpisodeNumber(
                user, 100, 1, 1)).thenReturn(true);
        when(episodeWatchRepository.existsByUserAndTmdbSeriesIdAndSeasonNumberAndEpisodeNumber(
                user, 100, 1, 2)).thenReturn(false);
        when(episodeWatchRepository.existsByUserAndTmdbSeriesIdAndSeasonNumberAndEpisodeNumber(
                user, 100, 1, 3)).thenReturn(false);

        episodeWatchService.markSeasonWatched(user, 100, 1, List.of(1, 2, 3));

        // Episode 1 already exists — only 2 and 3 should be saved
        verify(episodeWatchRepository, times(2)).save(any(EpisodeWatch.class));
    }

    @Test
    void markSeasonWatched_shouldSaveAll_whenNoneWatched() {
        User user = buildUser(1L);

        when(episodeWatchRepository.existsByUserAndTmdbSeriesIdAndSeasonNumberAndEpisodeNumber(
                eq(user), eq(100), eq(1), any())).thenReturn(false);

        episodeWatchService.markSeasonWatched(user, 100, 1, List.of(1, 2, 3, 4));

        verify(episodeWatchRepository, times(4)).save(any(EpisodeWatch.class));
    }

    @Test
    void markSeasonWatched_shouldSaveNone_whenAllAlreadyWatched() {
        User user = buildUser(1L);

        when(episodeWatchRepository.existsByUserAndTmdbSeriesIdAndSeasonNumberAndEpisodeNumber(
                eq(user), eq(100), eq(1), any())).thenReturn(true);

        episodeWatchService.markSeasonWatched(user, 100, 1, List.of(1, 2, 3));

        verify(episodeWatchRepository, never()).save(any());
    }

    // ---------- unmarkSeasonWatched ----------

    @Test
    void unmarkSeasonWatched_shouldDeleteEpisodesForSeason() {
        User user = buildUser(1L);
        EpisodeWatch ep1 = buildEpisode(user, 100, 2, 1);
        EpisodeWatch ep2 = buildEpisode(user, 100, 2, 2);
        EpisodeWatch ep3 = buildEpisode(user, 100, 3, 1); // different season

        when(episodeWatchRepository.findByUserAndTmdbSeriesId(user, 100))
                .thenReturn(List.of(ep1, ep2, ep3));

        episodeWatchService.unmarkSeasonWatched(user, 100, 2);

        verify(episodeWatchRepository).deleteAll(argThat(list ->
                ((java.util.Collection<?>) list).size() == 2));
    }

    @Test
    void unmarkSeasonWatched_shouldDeleteNothing_whenSeasonNotWatched() {
        User user = buildUser(1L);

        when(episodeWatchRepository.findByUserAndTmdbSeriesId(user, 100)).thenReturn(List.of());

        episodeWatchService.unmarkSeasonWatched(user, 100, 1);

        verify(episodeWatchRepository).deleteAll(argThat(list ->
                ((java.util.Collection<?>) list).isEmpty()));
    }

    // ---------- getSeriesProgress ----------

    @Test
    void getSeriesProgress_shouldReturnProgressWithContentInfo() {
        User user = buildUser(1L);
        Content content = buildContent(100, "Breaking Bad", "poster.jpg");

        when(episodeWatchRepository.findWatchedCountByUserGroupedBySeries(user))
                .thenReturn(List.of(new Object[]{100, 5L}));
        when(contentRepository.findByTmdbId(100)).thenReturn(Optional.of(content));

        List<SeriesProgressDto> result = episodeWatchService.getSeriesProgress(user);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).tmdbSeriesId()).isEqualTo(100);
        assertThat(result.get(0).title()).isEqualTo("Breaking Bad");
        assertThat(result.get(0).posterUrl()).isEqualTo("poster.jpg");
        assertThat(result.get(0).watchedCount()).isEqualTo(5L);
    }

    @Test
    void getSeriesProgress_shouldUseFallbackTitle_whenContentNotFound() {
        User user = buildUser(1L);

        when(episodeWatchRepository.findWatchedCountByUserGroupedBySeries(user))
                .thenReturn(List.of(new Object[]{999, 3L}));
        when(contentRepository.findByTmdbId(999)).thenReturn(Optional.empty());

        List<SeriesProgressDto> result = episodeWatchService.getSeriesProgress(user);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Serie 999");
        assertThat(result.get(0).posterUrl()).isNull();
    }

    @Test
    void getSeriesProgress_shouldReturnEmpty_whenNothingWatched() {
        User user = buildUser(1L);
        when(episodeWatchRepository.findWatchedCountByUserGroupedBySeries(user))
                .thenReturn(List.of());

        List<SeriesProgressDto> result = episodeWatchService.getSeriesProgress(user);

        assertThat(result).isEmpty();
    }

    // ---------- helpers ----------

    private User buildUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setUsername("user" + id);
        return u;
    }

    private EpisodeWatch buildEpisode(User user, Integer seriesId, Integer season, Integer episode) {
        EpisodeWatch e = new EpisodeWatch();
        e.setUser(user);
        e.setTmdbSeriesId(seriesId);
        e.setSeasonNumber(season);
        e.setEpisodeNumber(episode);
        return e;
    }

    private Content buildContent(Integer tmdbId, String title, String poster) {
        Content c = new Content();
        c.setTmdbId(tmdbId);
        c.setTitle(title);
        c.setPosterUrl(poster);
        c.setContentType(Content.ContentType.TV);
        return c;
    }
}
