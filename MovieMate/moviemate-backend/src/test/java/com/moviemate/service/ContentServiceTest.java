package com.moviemate.service;

import com.moviemate.dto.ContentResponse;
import com.moviemate.entity.Content;
import com.moviemate.repository.ContentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ContentServiceTest {

    private ContentRepository contentRepository;
    private TmdbService tmdbService;
    private ContentService contentService;

    @BeforeEach
    void setUp() {
        contentRepository = mock(ContentRepository.class);
        tmdbService = mock(TmdbService.class);
        contentService = new ContentService(contentRepository, tmdbService);
    }

    // ---------- getContentById ----------

    @Test
    void getContentById_shouldReturnContent_whenExists() {
        Content content = buildContent(1L, 100);
        when(contentRepository.findById(1L)).thenReturn(Optional.of(content));

        ContentResponse response = contentService.getContentById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTmdbId()).isEqualTo(100);
        verify(contentRepository).findById(1L);
    }

    @Test
    void getContentById_shouldThrow_whenNotFound() {
        when(contentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contentService.getContentById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Contenido no encontrado");

        verify(contentRepository).findById(999L);
    }

    // ---------- getByTmdbId ----------

    @Test
    void getByTmdbId_shouldReturnContent_whenExists() {
        Content content = buildContent(1L, 100);
        when(contentRepository.findByTmdbId(100)).thenReturn(Optional.of(content));

        ContentResponse response = contentService.getByTmdbId(100);

        assertThat(response).isNotNull();
        assertThat(response.getTmdbId()).isEqualTo(100);
        verify(contentRepository).findByTmdbId(100);
    }

    @Test
    void getByTmdbId_shouldThrow_whenNotFound() {
        when(contentRepository.findByTmdbId(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contentService.getByTmdbId(999))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Contenido de TMDB no encontrado");

        verify(contentRepository).findByTmdbId(999);
    }

    // ---------- getAllContent ----------

    @Test
    void getAllContent_shouldReturnAllContent() {
        Content content1 = buildContent(1L, 100);
        Content content2 = buildContent(2L, 200);

        when(contentRepository.findAll()).thenReturn(List.of(content1, content2));

        List<ContentResponse> responses = contentService.getAllContent();

        assertThat(responses).hasSize(2);
        assertThat(responses)
                .extracting(ContentResponse::getTmdbId)
                .containsExactlyInAnyOrder(100, 200);

        verify(contentRepository).findAll();
    }

    @Test
    void getAllContent_shouldReturnEmpty_whenNoContent() {
        when(contentRepository.findAll()).thenReturn(List.of());

        List<ContentResponse> responses = contentService.getAllContent();

        assertThat(responses).isEmpty();
        verify(contentRepository).findAll();
    }

    // ---------- getOrFetch ----------

    @Test
    void getOrFetch_shouldReturnExisting_whenContentExists() {
        Content content = buildContent(1L, 100);
        content.setLastTmdbSync(LocalDateTime.now().minusDays(3)); // Fresh
        content.setLastInteraction(LocalDateTime.now().minusDays(1));

        when(contentRepository.findByTmdbId(100)).thenReturn(Optional.of(content));

        // SIN type parameter
        Content result = contentService.getOrFetch(100);

        assertThat(result).isNotNull();
        assertThat(result.getTmdbId()).isEqualTo(100);
        assertThat(result.getLastInteraction()).isNotNull();

        verify(contentRepository).findByTmdbId(100);
        verify(tmdbService, never()).syncMovieFromTmdb(any());
        verify(tmdbService, never()).syncTvShowFromTmdb(any());
        verify(contentRepository, never()).save(any()); // No save (cache hit)
    }

    @Test
    void getOrFetch_shouldFetchFromTmdb_whenContentDoesNotExist() {
        Content newContent = buildContent(1L, 100);

        when(contentRepository.findByTmdbId(100)).thenReturn(Optional.empty());
        when(tmdbService.syncMovieFromTmdb(100)).thenReturn(newContent);

        Content result = contentService.getOrFetch(100);

        assertThat(result).isNotNull();
        assertThat(result.getTmdbId()).isEqualTo(100);
        assertThat(result.getLastTmdbSync()).isNotNull();
        assertThat(result.getLastInteraction()).isNotNull();

        verify(contentRepository).findByTmdbId(100);
        verify(tmdbService).syncMovieFromTmdb(100);
        verify(contentRepository, never()).save(any()); // TmdbService ya persiste; no hay segundo save
    }

    @Test
    void getOrFetch_shouldFetchTvShow_whenMovieFails() {
        Content tvShow = buildContent(1L, 200);
        tvShow.setContentType(Content.ContentType.TV);

        when(contentRepository.findByTmdbId(200)).thenReturn(Optional.empty());
        when(tmdbService.syncMovieFromTmdb(200)).thenThrow(new RuntimeException("not a movie"));
        when(tmdbService.syncTvShowFromTmdb(200)).thenReturn(tvShow);

        Content result = contentService.getOrFetch(200);

        assertThat(result).isNotNull();
        assertThat(result.getContentType()).isEqualTo(Content.ContentType.TV);

        verify(tmdbService).syncMovieFromTmdb(200);  // tried first
        verify(tmdbService).syncTvShowFromTmdb(200); // fallback
    }

    @Test
    void getOrFetch_shouldTriggerRefresh_whenContentIsStale() {
        Content staleContent = buildContent(1L, 100);
        staleContent.setLastTmdbSync(LocalDateTime.now().minusDays(10)); // Stale > TTL

        when(contentRepository.findByTmdbId(100)).thenReturn(Optional.of(staleContent));

        Content result = contentService.getOrFetch(100);

        assertThat(result).isNotNull();
        assertThat(result.getLastInteraction()).isAfterOrEqualTo(LocalDateTime.now().minusSeconds(1));
        
        // refreshAsync() asíncrono → verify eventual
        verify(contentRepository).findByTmdbId(100);
    }

    @Test
    void getOrFetch_shouldTriggerRefresh_whenLastSyncIsNull() {
        Content content = buildContent(1L, 100);
        content.setLastTmdbSync(null); // Nunca sync

        when(contentRepository.findByTmdbId(100)).thenReturn(Optional.of(content));

        Content result = contentService.getOrFetch(100);

        assertThat(result).isNotNull();
        verify(contentRepository).findByTmdbId(100);
    }

    @Test
    void getOrFetch_shouldThrow_whenBothMovieAndTvFail() {
        when(contentRepository.findByTmdbId(999)).thenReturn(Optional.empty());
        when(tmdbService.syncMovieFromTmdb(999)).thenThrow(new RuntimeException("TMDB movie fail"));
        when(tmdbService.syncTvShowFromTmdb(999)).thenThrow(new RuntimeException("TMDB tv fail"));

        assertThrows(RuntimeException.class, () -> contentService.getOrFetch(999));
    }

    // ---------- refreshAsync ----------

    @Test
    void refreshAsync_shouldUpdateContent_whenContentExists() {
        Content content = buildContent(1L, 100);
        content.setSyncStatus(Content.SyncStatus.FRESH);
        
        Content updatedContent = buildContent(1L, 100);
        updatedContent.setTitle("Updated Title");
        updatedContent.setTmdbRating(9.5);

        when(contentRepository.findById(1L)).thenReturn(Optional.of(content));
        when(tmdbService.syncMovieFromTmdb(100)).thenReturn(updatedContent);

        contentService.refreshAsync(1L);

        // Dar tiempo para que se ejecute el async (en tests reales, mejor usar await)
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(contentRepository).findById(1L);
        verify(tmdbService).syncMovieFromTmdb(100);
    }

    @Test
    void refreshAsync_shouldDoNothing_whenContentNotFound() {
        when(contentRepository.findById(999L)).thenReturn(Optional.empty());

        contentService.refreshAsync(999L);

        verify(contentRepository).findById(999L);
        verify(tmdbService, never()).syncMovieFromTmdb(any());
        verify(tmdbService, never()).syncTvShowFromTmdb(any());
    }

    @Test
    void refreshAsync_shouldDoNothing_whenAlreadyUpdating() {
        Content content = buildContent(1L, 100);
        content.setSyncStatus(Content.SyncStatus.UPDATING);

        when(contentRepository.findById(1L)).thenReturn(Optional.of(content));

        contentService.refreshAsync(1L);

        verify(contentRepository).findById(1L);
        verify(tmdbService, never()).syncMovieFromTmdb(any());
    }

    @Test
    void refreshAsync_shouldUpdateTvShow_whenContentTypeIsTvShow() {
        Content tvShow = buildContent(1L, 200);
        tvShow.setContentType(Content.ContentType.TV);
        tvShow.setSyncStatus(Content.SyncStatus.FRESH);

        Content updatedTvShow = buildContent(1L, 200);
        updatedTvShow.setContentType(Content.ContentType.TV);

        when(contentRepository.findById(1L)).thenReturn(Optional.of(tvShow));
        when(tmdbService.syncTvShowFromTmdb(200)).thenReturn(updatedTvShow);

        contentService.refreshAsync(1L);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(tmdbService).syncTvShowFromTmdb(200);
        verify(tmdbService, never()).syncMovieFromTmdb(any());
    }

    // ---------- mapToContentResponse ----------

    @Test
    void mapToContentResponse_shouldMapAllFields() {
        Content content = buildContent(1L, 100);

        ContentResponse response = contentService.mapToContentResponse(content);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTmdbId()).isEqualTo(100);
        assertThat(response.getTitle()).isEqualTo("Test Movie");
        assertThat(response.getContentType()).isEqualTo(Content.ContentType.MOVIE);
        assertThat(response.getReleaseDate()).isEqualTo("2020-01-01");
        assertThat(response.getPosterUrl()).isEqualTo("poster.jpg");
        assertThat(response.getBackdropUrl()).isEqualTo("backdrop.jpg");
        assertThat(response.getSynopsis()).isEqualTo("Synopsis");
        assertThat(response.getGenres()).containsExactly("Action", "Adventure");
        assertThat(response.getTmdbRating()).isEqualTo(8.5);
        assertThat(response.getTmdbVoteCount()).isEqualTo(1000);
        assertThat(response.getAppRating()).isEqualTo(9.0);
        assertThat(response.getAppVoteCount()).isEqualTo(100);
    }

    @Test
    void mapToContentResponse_shouldHandleNullReleaseDate() {
        Content content = buildContent(1L, 100);
        content.setReleaseDate(null);

        ContentResponse response = contentService.mapToContentResponse(content);

        assertThat(response.getReleaseDate()).isNull();
    }

    // ---------- helpers ----------

    private Content buildContent(Long id, Integer tmdbId) {
        Content c = new Content();
        c.setId(id);
        c.setTmdbId(tmdbId);
        c.setTitle("Test Movie");
        c.setContentType(Content.ContentType.MOVIE);
        c.setReleaseDate(LocalDate.of(2020, 1, 1));
        c.setPosterUrl("poster.jpg");
        c.setBackdropUrl("backdrop.jpg");
        c.setSynopsis("Synopsis");
        c.setGenres(new java.util.ArrayList<>(List.of("Action", "Adventure")));
        c.setTmdbRating(8.5);
        c.setTmdbVoteCount(1000);
        c.setAppRating(9.0);
        c.setAppVoteCount(100);
        c.setLastTmdbSync(LocalDateTime.now().minusDays(1));
        c.setLastInteraction(LocalDateTime.now().minusHours(5));
        c.setSyncStatus(Content.SyncStatus.FRESH);
        return c;
    }
}