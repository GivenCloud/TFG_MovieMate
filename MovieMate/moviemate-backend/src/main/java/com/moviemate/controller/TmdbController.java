package com.moviemate.controller;

import com.moviemate.dto.CastMemberDto;
import com.moviemate.dto.ContentResponse;
import com.moviemate.dto.GenreDto;
import com.moviemate.dto.PersonDto;
import com.moviemate.dto.SeasonDto;
import com.moviemate.dto.SeasonSummaryDto;
import com.moviemate.dto.WatchProvidersDto;
import com.moviemate.entity.Content;
import com.moviemate.service.ContentService;
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
    private final ContentService contentService;

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
            summary = "Obtener contenido trending",
            description = "Devuelve una lista mixta de películas y series en tendencia esta semana."
    )
    @GetMapping("/trending")
    public ResponseEntity<List<Content>> getTrending(
            @Parameter(description = "Número de página", example = "1")
            @RequestParam(required = false, defaultValue = "1") Integer page
    ) {
        return ResponseEntity.ok(tmdbService.getTrendingAll(page));
    }

    @Operation(summary = "Géneros de películas", description = "Devuelve la lista de géneros de películas de TMDB.")
    @GetMapping("/genres/movies")
    public ResponseEntity<List<GenreDto>> getMovieGenres() {
        return ResponseEntity.ok(tmdbService.getMovieGenres());
    }

    @Operation(summary = "Géneros de series", description = "Devuelve la lista de géneros de series de TMDB.")
    @GetMapping("/genres/tv")
    public ResponseEntity<List<GenreDto>> getTvGenres() {
        return ResponseEntity.ok(tmdbService.getTvGenres());
    }

    @Operation(summary = "Descubrir películas con filtros", description = "Proxy a TMDB Discover para películas con filtros de género, año, puntuación y orden.")
    @GetMapping("/discover/movies")
    public ResponseEntity<List<Content>> discoverMovies(
            @RequestParam(required = false) Integer genre,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false, defaultValue = "popularity.desc") String sortBy,
            @RequestParam(required = false, defaultValue = "1") Integer page
    ) {
        return ResponseEntity.ok(tmdbService.discoverMovies(genre, year, minRating, sortBy, page));
    }

    @Operation(summary = "Descubrir series con filtros", description = "Proxy a TMDB Discover para series con filtros de género, año, puntuación y orden.")
    @GetMapping("/discover/tv")
    public ResponseEntity<List<Content>> discoverTvShows(
            @RequestParam(required = false) Integer genre,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false, defaultValue = "popularity.desc") String sortBy,
            @RequestParam(required = false, defaultValue = "1") Integer page
    ) {
        return ResponseEntity.ok(tmdbService.discoverTvShows(genre, year, minRating, sortBy, page));
    }

    @Operation(summary = "Perfil de persona", description = "Devuelve datos de una persona (actor, director, etc.) desde TMDB.")
    @GetMapping("/people/{personId}")
    public ResponseEntity<PersonDto> getPersonDetails(@PathVariable Integer personId) {
        PersonDto person = tmdbService.getPersonDetails(personId);
        if (person == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(person);
    }

    @Operation(summary = "Filmografía de persona", description = "Devuelve las películas y series en las que aparece una persona.")
    @GetMapping("/people/{personId}/credits")
    public ResponseEntity<List<Content>> getPersonCredits(@PathVariable Integer personId) {
        return ResponseEntity.ok(tmdbService.getPersonCredits(personId));
    }

    @Operation(summary = "Cast de un contenido", description = "Devuelve el reparto principal y director de una película o serie.")
    @GetMapping("/{contentType}/{tmdbId}/credits")
    public ResponseEntity<List<CastMemberDto>> getContentCredits(
            @PathVariable String contentType,
            @PathVariable Integer tmdbId
    ) {
        return ResponseEntity.ok(tmdbService.getContentCredits(tmdbId, contentType));
    }

    @Operation(
            summary = "Proveedores de streaming (¿Dónde ver?)",
            description = "Devuelve las plataformas donde está disponible el contenido (streaming, alquiler, compra). Prioriza España (ES), luego US."
    )
    @GetMapping("/{contentType}/{tmdbId}/providers")
    public ResponseEntity<WatchProvidersDto> getWatchProviders(
            @PathVariable String contentType,
            @PathVariable Integer tmdbId
    ) {
        return ResponseEntity.ok(tmdbService.getWatchProviders(tmdbId, contentType));
    }

    @Operation(
            summary = "Sincronizar película desde TMDB",
            description = "Obtiene información detallada de una película desde TMDB y la guarda en la base de datos."
    )
    @PostMapping("/movies/{tmdbId}/sync")
    public ResponseEntity<ContentResponse> syncMovie(
            @Parameter(
                    description = "ID de película en TMDB",
                    examples = {
                            @ExampleObject(name = "Ejemplo TMDB Movie ID", value = "157336") // Interstellar
                    }
            )
            @PathVariable Integer tmdbId
    ) {
        Content content = tmdbService.syncMovieFromTmdb(tmdbId);
        return ResponseEntity.ok(contentService.mapToContentResponse(content));
    }

    @Operation(summary = "Obtener resumen de temporadas de una serie")
    @GetMapping("/tv/{tmdbId}/seasons")
    public ResponseEntity<List<SeasonSummaryDto>> getTvSeasonsSummary(@PathVariable Integer tmdbId) {
        return ResponseEntity.ok(tmdbService.getTvSeasonsSummary(tmdbId));
    }

    @Operation(summary = "Obtener episodios de una temporada")
    @GetMapping("/tv/{tmdbId}/seasons/{seasonNumber}")
    public ResponseEntity<SeasonDto> getSeasonDetails(
            @PathVariable Integer tmdbId,
            @PathVariable Integer seasonNumber) {
        SeasonDto season = tmdbService.getSeasonDetails(tmdbId, seasonNumber);
        if (season == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(season);
    }

    @Operation(
            summary = "Sincronizar serie desde TMDB",
            description = "Obtiene información detallada de una serie desde TMDB y la guarda en la base de datos."
    )
    @PostMapping("/tv/{tmdbId}/sync")
    public ResponseEntity<ContentResponse> syncTvShow(
            @Parameter(
                    description = "ID de serie en TMDB",
                    examples = {
                            @ExampleObject(name = "Ejemplo TMDB TV ID", value = "1399") // Game of Thrones
                    }
            )
            @PathVariable Integer tmdbId
    ) {
        Content content = tmdbService.syncTvShowFromTmdb(tmdbId);
        return ResponseEntity.ok(contentService.mapToContentResponse(content));
    }
}
