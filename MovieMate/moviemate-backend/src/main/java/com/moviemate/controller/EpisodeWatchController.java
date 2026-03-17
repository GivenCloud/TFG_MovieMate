package com.moviemate.controller;

import com.moviemate.entity.User;
import com.moviemate.security.CustomUserDetails;
import com.moviemate.service.EpisodeWatchService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/episodes")
@RequiredArgsConstructor
public class EpisodeWatchController {

    private final EpisodeWatchService episodeWatchService;

    @Operation(summary = "Obtener episodios vistos de una serie")
    @GetMapping("/watched/{tmdbSeriesId}")
    public ResponseEntity<Set<String>> getWatchedEpisodes(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer tmdbSeriesId) {
        User user = userDetails.getUser();
        return ResponseEntity.ok(episodeWatchService.getWatchedEpisodes(user, tmdbSeriesId));
    }

    @Operation(summary = "Marcar/desmarcar un episodio como visto")
    @PostMapping("/watched/{tmdbSeriesId}/{seasonNumber}/{episodeNumber}")
    public ResponseEntity<Boolean> toggleEpisodeWatched(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer tmdbSeriesId,
            @PathVariable Integer seasonNumber,
            @PathVariable Integer episodeNumber) {
        User user = userDetails.getUser();
        boolean nowWatched = episodeWatchService.toggleEpisodeWatched(
                user, tmdbSeriesId, seasonNumber, episodeNumber);
        return ResponseEntity.ok(nowWatched);
    }

    @Operation(summary = "Marcar todos los episodios de una temporada como vistos")
    @PostMapping("/watched/{tmdbSeriesId}/{seasonNumber}/all")
    public ResponseEntity<Void> markSeasonWatched(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer tmdbSeriesId,
            @PathVariable Integer seasonNumber,
            @RequestBody List<Integer> episodeNumbers) {
        User user = userDetails.getUser();
        episodeWatchService.markSeasonWatched(user, tmdbSeriesId, seasonNumber, episodeNumbers);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Desmarcar todos los episodios de una temporada")
    @DeleteMapping("/watched/{tmdbSeriesId}/{seasonNumber}/all")
    public ResponseEntity<Void> unmarkSeasonWatched(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer tmdbSeriesId,
            @PathVariable Integer seasonNumber) {
        User user = userDetails.getUser();
        episodeWatchService.unmarkSeasonWatched(user, tmdbSeriesId, seasonNumber);
        return ResponseEntity.ok().build();
    }
}
