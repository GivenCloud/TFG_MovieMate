package com.moviemate.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moviemate.dto.ContentResponse;
import com.moviemate.entity.Content;
import com.moviemate.repository.ContentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final TmdbService tmdbService;

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

    public ContentResponse addContent(Content content) {
        return mapToContentResponse(contentRepository.save(content));
    }

    @Transactional
    public Content getOrSyncByTmdb(Integer tmdbId, Content.ContentType contentType) {
        return contentRepository.findByTmdbId(tmdbId)
                .orElseGet(() -> {
                    if (contentType == Content.ContentType.MOVIE) {
                        return tmdbService.syncMovieFromTmdb(tmdbId);
                    } else {
                        return tmdbService.syncTvShowFromTmdb(tmdbId);
                    }
                });
    }

    public ContentResponse mapToContentResponse(Content content) {
        return ContentResponse.builder()
                .id(content.getId())
                .tmdbId(content.getTmdbId())
                .title(content.getTitle())
                .contentType(content.getContentType())
                .releaseDate(content.getReleaseDate().toString())
                .posterUrl(content.getPosterUrl())
                .backdropUrl(content.getBackdropUrl())
                .synopsis(content.getSynopsis())
                .genres(content.getGenres())
                .averageRating(content.getAverageRating())
                .voteCount(content.getVoteCount())
                .lastSync(content.getLastSync().toString())
                .build();
    }
}
