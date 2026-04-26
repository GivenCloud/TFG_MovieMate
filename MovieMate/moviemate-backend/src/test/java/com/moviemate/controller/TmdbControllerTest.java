package com.moviemate.controller;

import com.moviemate.dto.ContentResponse;
import com.moviemate.dto.PersonDto;
import com.moviemate.dto.SeasonDto;
import com.moviemate.entity.Content;
import com.moviemate.service.ContentService;
import com.moviemate.service.TmdbService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TmdbControllerTest {

    private TmdbService tmdbService;
    private ContentService contentService;
    private TmdbController tmdbController;

    @BeforeEach
    void setUp() {
        tmdbService = mock(TmdbService.class);
        contentService = mock(ContentService.class);
        tmdbController = new TmdbController(tmdbService, contentService);
    }

    @Test
    void searchMovies_shouldReturnOk() {
        List<Content> payload = List.of(new Content());
        when(tmdbService.searchMovies("matrix", 1)).thenReturn(payload);

        ResponseEntity<List<Content>> response = tmdbController.searchMovies("matrix", 1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(payload);
    }

    @Test
    void getPopularMovies_shouldReturnOk() {
        List<Content> payload = List.of(new Content());
        when(tmdbService.getPopularMovies(1)).thenReturn(payload);

        ResponseEntity<List<Content>> response = tmdbController.getPopularMovies(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(payload);
    }

    @Test
    void searchTvShows_shouldReturnOk() {
        List<Content> payload = List.of(new Content());
        when(tmdbService.searchTvShows("dark", 1)).thenReturn(payload);

        ResponseEntity<List<Content>> response = tmdbController.searchTvShows("dark", 1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(payload);
    }

    @Test
    void getPopularTvShows_shouldReturnOk() {
        List<Content> payload = List.of(new Content());
        when(tmdbService.getPopularTvShows(1)).thenReturn(payload);

        ResponseEntity<List<Content>> response = tmdbController.getPopularTvShows(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(payload);
    }

    @Test
    void getTrending_shouldReturnOk() {
        List<Content> payload = List.of(new Content());
        when(tmdbService.getTrendingAll(1)).thenReturn(payload);

        ResponseEntity<List<Content>> response = tmdbController.getTrending(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(payload);
    }

    @Test
    void getMovieGenres_shouldReturnOk() {
        List payload = List.of();
        when(tmdbService.getMovieGenres()).thenReturn(payload);

        ResponseEntity<List<com.moviemate.dto.GenreDto>> response = tmdbController.getMovieGenres();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(payload);
    }

    @Test
    void getTvGenres_shouldReturnOk() {
        List payload = List.of();
        when(tmdbService.getTvGenres()).thenReturn(payload);

        ResponseEntity<List<com.moviemate.dto.GenreDto>> response = tmdbController.getTvGenres();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(payload);
    }

    @Test
    void discoverMovies_shouldReturnOk() {
        List<Content> payload = List.of(new Content());
        when(tmdbService.discoverMovies(28, 2020, 7.0, "vote_average.desc", 1)).thenReturn(payload);

        ResponseEntity<List<Content>> response = tmdbController.discoverMovies(28, 2020, 7.0, "vote_average.desc", 1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(payload);
    }

    @Test
    void discoverTvShows_shouldReturnOk() {
        List<Content> payload = List.of(new Content());
        when(tmdbService.discoverTvShows(18, 2021, 7.0, "popularity.desc", 1)).thenReturn(payload);

        ResponseEntity<List<Content>> response = tmdbController.discoverTvShows(18, 2021, 7.0, "popularity.desc", 1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(payload);
    }

    @Test
    void getPersonDetails_shouldReturnNotFoundWhenNull() {
        when(tmdbService.getPersonDetails(10)).thenReturn(null);

        ResponseEntity<PersonDto> response = tmdbController.getPersonDetails(10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getPersonDetails_shouldReturnOkWhenPresent() {
        PersonDto person = mock(PersonDto.class);
        when(tmdbService.getPersonDetails(10)).thenReturn(person);

        ResponseEntity<PersonDto> response = tmdbController.getPersonDetails(10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(person);
    }

    @Test
    void getPersonCredits_shouldReturnOk() {
        List<Content> payload = List.of(new Content());
        when(tmdbService.getPersonCredits(10)).thenReturn(payload);

        ResponseEntity<List<Content>> response = tmdbController.getPersonCredits(10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(payload);
    }

    @Test
    void getContentCredits_shouldReturnOk() {
        List payload = List.of();
        when(tmdbService.getContentCredits(100, "movie")).thenReturn(payload);

        ResponseEntity<List<com.moviemate.dto.CastMemberDto>> response = tmdbController.getContentCredits("movie", 100);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(payload);
    }

    @Test
    void getWatchProviders_shouldReturnOk() {
        com.moviemate.dto.WatchProvidersDto payload = mock(com.moviemate.dto.WatchProvidersDto.class);
        when(tmdbService.getWatchProviders(100, "movie")).thenReturn(payload);

        ResponseEntity<com.moviemate.dto.WatchProvidersDto> response = tmdbController.getWatchProviders("movie", 100);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(payload);
    }

    @Test
    void syncMovie_shouldReturnOk() {
        Content content = new Content();
        ContentResponse mapped = ContentResponse.builder().id(10L).title("Movie").build();
        when(tmdbService.syncMovieFromTmdb(100)).thenReturn(content);
        when(contentService.mapToContentResponse(content)).thenReturn(mapped);

        ResponseEntity<ContentResponse> response = tmdbController.syncMovie(100);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mapped);
    }

    @Test
    void getTvSeasonsSummary_shouldReturnOk() {
        List payload = List.of();
        when(tmdbService.getTvSeasonsSummary(100)).thenReturn(payload);

        ResponseEntity<List<com.moviemate.dto.SeasonSummaryDto>> response = tmdbController.getTvSeasonsSummary(100);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(payload);
    }

    @Test
    void getSeasonDetails_shouldHandleNullAndNonNull() {
        when(tmdbService.getSeasonDetails(100, 1)).thenReturn(null);
        ResponseEntity<SeasonDto> notFound = tmdbController.getSeasonDetails(100, 1);
        assertThat(notFound.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        SeasonDto season = mock(SeasonDto.class);
        when(tmdbService.getSeasonDetails(100, 1)).thenReturn(season);
        ResponseEntity<SeasonDto> ok = tmdbController.getSeasonDetails(100, 1);
        assertThat(ok.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ok.getBody()).isEqualTo(season);
    }

    @Test
    void syncTvShow_shouldReturnOk() {
        Content content = new Content();
        ContentResponse mapped = ContentResponse.builder().id(11L).title("Show").build();
        when(tmdbService.syncTvShowFromTmdb(1399)).thenReturn(content);
        when(contentService.mapToContentResponse(content)).thenReturn(mapped);

        ResponseEntity<ContentResponse> response = tmdbController.syncTvShow(1399);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mapped);
    }
}
