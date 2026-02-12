package com.moviemate.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moviemate.dto.ContentResponse;
import com.moviemate.entity.Content;
import com.moviemate.repository.ContentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@EnableAsync
public class ContentService {

    private final ContentRepository contentRepository;
    private final TmdbService tmdbService;

    private static final int TTL_DAYS = 7;

    public ContentResponse getContentById(Long id) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contenido no encontrado"));
        return mapToContentResponse(content);
    }

    public ContentResponse getByTmdbId(Integer tmdbId) {
        Content content = contentRepository.findByTmdbId(tmdbId)
                .orElseThrow(() -> new RuntimeException("Contenido de TMDB no encontrado"));
        return mapToContentResponse(content);
    }

    public List<ContentResponse> getAllContent() {
        return contentRepository.findAll().stream()
                .map(this::mapToContentResponse)
                .toList();
    }

    @Transactional
    public Content getOrFetch(Integer tmdbId, Content.ContentType type) {
        Content content = contentRepository.findByTmdbId(tmdbId)
                .orElseGet(() -> fetchFromTmdb(tmdbId, type));

        content.setLastInteraction(LocalDateTime.now());

        if (isStale(content)) {
            refreshAsync(content.getId());
        }

        return content;
    }

    private boolean isStale(Content content) {
        if (content.getLastTmdbSync() == null)
            return true;

        return content.getLastTmdbSync()
                .isBefore(LocalDateTime.now().minusDays(TTL_DAYS));
    }

    private Content fetchFromTmdb(Integer tmdbId, Content.ContentType type) {
        Content content = (type == Content.ContentType.MOVIE)
                ? tmdbService.syncMovieFromTmdb(tmdbId)
                : tmdbService.syncTvShowFromTmdb(tmdbId);

        content.setLastTmdbSync(LocalDateTime.now());
        content.setLastInteraction(LocalDateTime.now());

        return contentRepository.save(content);
    }

    @Async
    @Transactional
    public void refreshAsync(Long contentId) {
        Content content = contentRepository.findById(contentId).orElse(null);
        if (content == null) return;

        if (content.getSyncStatus() == Content.SyncStatus.UPDATING)
            return;

        content.setSyncStatus(Content.SyncStatus.UPDATING);

        Content updated = (content.getContentType() == Content.ContentType.MOVIE)
                ? tmdbService.syncMovieFromTmdb(content.getTmdbId())
                : tmdbService.syncTvShowFromTmdb(content.getTmdbId());

        mapSnapshot(content, updated);

        content.setLastTmdbSync(LocalDateTime.now());
        content.setSyncStatus(Content.SyncStatus.FRESH);
    }

    private void mapSnapshot(Content target, Content source) {
        target.setTitle(source.getTitle());
        target.setSynopsis(source.getSynopsis());
        target.setPosterUrl(source.getPosterUrl());
        target.setBackdropUrl(source.getBackdropUrl());
        target.setReleaseDate(source.getReleaseDate());
        target.setGenres(source.getGenres());
        target.setTmdbRating(source.getTmdbRating());
        target.setTmdbVoteCount(source.getTmdbVoteCount());
    }

    public ContentResponse mapToContentResponse(Content content) {
        return ContentResponse.builder()
                .id(content.getId())
                .tmdbId(content.getTmdbId())
                .title(content.getTitle())
                .contentType(content.getContentType())
                .releaseDate(content.getReleaseDate() != null ? content.getReleaseDate().toString() : null)
                .posterUrl(content.getPosterUrl())
                .backdropUrl(content.getBackdropUrl())
                .synopsis(content.getSynopsis())
                .genres(content.getGenres())
                .tmdbRating(content.getTmdbRating())
                .tmdbVoteCount(content.getTmdbVoteCount())
                .appRating(content.getAppRating())
                .appVoteCount(content.getAppVoteCount())
                .build();
    }
}
