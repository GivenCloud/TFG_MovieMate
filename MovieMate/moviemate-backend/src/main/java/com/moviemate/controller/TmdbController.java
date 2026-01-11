package com.moviemate.controller;

import com.moviemate.entity.Content;
import com.moviemate.service.TmdbService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@RestController
@RequestMapping("/api/tmdb")
@RequiredArgsConstructor
public class TmdbController {

    private final TmdbService tmdbService;

    @Operation(
            summary = "Buscar películas por nombre",
            description = "Realiza una búsqueda de películas usando la API de TMDB."
    )
    @GetMapping("/movies")
    public ResponseEntity<List<Content>> searchMovies(
            @Parameter(
                    description = "Texto de búsqueda",
                    example = "Inception"
            )
            @RequestParam String query,

            @Parameter(
                    description = "Número de página",
                    example = "1"
            )
            @RequestParam(required = false, defaultValue = "1") Integer page
    ) {
        return ResponseEntity.ok(tmdbService.searchMovies(query, page));
    }

    @Operation(
            summary = "Obtener películas populares",
            description = "Devuelve una lista de películas más populares actualmente en TMDB."
    )
    @GetMapping("/movies/popular")
    public ResponseEntity<List<Content>> getPopularMovies(
            @Parameter(
                    description = "Número de página",
                    example = "1"
            )
            @RequestParam(required = false, defaultValue = "1") Integer page
    ) {
        return ResponseEntity.ok(tmdbService.getPopularMovies(page));
    }

    @Operation(
            summary = "Buscar series por nombre",
            description = "Realiza una búsqueda de series usando la API de TMDB."
    )
    @GetMapping("/tv")
    public ResponseEntity<List<Content>> searchTvShows(
            @Parameter(
                    description = "Texto de búsqueda",
                    example = "Game of Thrones"
            )
            @RequestParam String query,

            @Parameter(
                    description = "Número de página",
                    example = "1"
            )
            @RequestParam(required = false, defaultValue = "1") Integer page
    ) {
        return ResponseEntity.ok(tmdbService.searchTvShows(query, page));
    }

    @Operation(
            summary = "Obtener series populares",
            description = "Devuelve una lista de series más populares en TMDB."
    )
    @GetMapping("/tv/popular")
    public ResponseEntity<List<Content>> getPopularTvShows(
            @Parameter(
                    description = "Número de página",
                    example = "1"
            )
            @RequestParam(required = false, defaultValue = "1") Integer page
    ) {
        return ResponseEntity.ok(tmdbService.getPopularTvShows(page));
    }

    @Operation(
            summary = "Sincronizar película desde TMDB",
            description = "Obtiene información detallada de una película desde TMDB y la guarda en la base de datos."
    )
    @PostMapping("/sync/movies/{tmdbId}")
    public ResponseEntity<Content> syncMovie(
            @Parameter(
                    description = "ID de película en TMDB",
                    examples = {
                            @ExampleObject(name = "Ejemplo TMDB Movie ID", value = "157336") // Interstellar
                    }
            )
            @PathVariable Integer tmdbId
    ) {
        Content content = tmdbService.syncMovieFromTmdb(tmdbId);
        return ResponseEntity.ok(content);
    }

    @Operation(
            summary = "Sincronizar serie desde TMDB",
            description = "Obtiene información detallada de una serie desde TMDB y la guarda en la base de datos."
    )
    @PostMapping("/sync/tv/{tmdbId}")
    public ResponseEntity<Content> syncTvShow(
            @Parameter(
                    description = "ID de serie en TMDB",
                    examples = {
                            @ExampleObject(name = "Ejemplo TMDB TV ID", value = "1399") // Game of Thrones
                    }
            )
            @PathVariable Integer tmdbId
    ) {
        Content content = tmdbService.syncTvShowFromTmdb(tmdbId);
        return ResponseEntity.ok(content);
    }
}
