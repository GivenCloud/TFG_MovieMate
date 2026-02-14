package com.moviemate.service;

import com.moviemate.dto.tmdb.TmdbMovieDetails;
import com.moviemate.dto.tmdb.TmdbSearchResponse;
import com.moviemate.dto.tmdb.TmdbTvDetails;
import com.moviemate.entity.Content;
import com.moviemate.repository.ContentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TmdbServiceTest {

    private RestTemplate restTemplate;
    private ContentRepository contentRepository;
    private TmdbService tmdbService;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        contentRepository = mock(ContentRepository.class);
        tmdbService = new TmdbService(restTemplate, contentRepository);
        
        // Configurar propiedades usando ReflectionTestUtils
        ReflectionTestUtils.setField(tmdbService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(tmdbService, "baseUrl", "https://api.themoviedb.org/3");
        ReflectionTestUtils.setField(tmdbService, "imageBaseUrl", "https://image.tmdb.org/t/p");
        ReflectionTestUtils.setField(tmdbService, "language", "es-ES");
    }

    // ---------- syncMovieFromTmdb ----------

    @Test
    void syncMovieFromTmdb_shouldReturnExisting_whenFound() {
        Content existingContent = buildContent(1L, 100, Content.ContentType.MOVIE);
        when(contentRepository.findByTmdbId(100)).thenReturn(Optional.of(existingContent));

        Content result = tmdbService.syncMovieFromTmdb(100);

        assertThat(result).isNotNull();
        assertThat(result.getTmdbId()).isEqualTo(100);
        verify(contentRepository).findByTmdbId(100);
        verify(restTemplate, never()).getForObject(anyString(), any());
    }

    @Test
    void syncMovieFromTmdb_shouldFetchFromTmdb_whenNotFound() {
        when(contentRepository.findByTmdbId(100)).thenReturn(Optional.empty());

        TmdbMovieDetails movieDetails = buildTmdbMovieDetails(100, "Test Movie");
        when(restTemplate.getForObject(anyString(), eq(TmdbMovieDetails.class)))
                .thenReturn(movieDetails);

        Content savedContent = buildContent(1L, 100, Content.ContentType.MOVIE);
        when(contentRepository.save(any(Content.class))).thenReturn(savedContent);

        Content result = tmdbService.syncMovieFromTmdb(100);

        assertThat(result).isNotNull();
        verify(contentRepository).findByTmdbId(100);
        verify(restTemplate).getForObject(anyString(), eq(TmdbMovieDetails.class));
        verify(contentRepository).save(any(Content.class));
    }

    @Test
    void syncMovieFromTmdb_shouldThrow_whenTmdbApiError() {
        when(contentRepository.findByTmdbId(100)).thenReturn(Optional.empty());
        when(restTemplate.getForObject(anyString(), eq(TmdbMovieDetails.class)))
                .thenThrow(new RuntimeException("TMDB API Error"));

        assertThatThrownBy(() -> tmdbService.syncMovieFromTmdb(100))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se pudo obtener la película de TMDB");
    }

    // ---------- syncTvShowFromTmdb ----------

    @Test
    void syncTvShowFromTmdb_shouldReturnExisting_whenFound() {
        Content existingContent = buildContent(1L, 200, Content.ContentType.TV);
        when(contentRepository.findByTmdbId(200)).thenReturn(Optional.of(existingContent));

        Content result = tmdbService.syncTvShowFromTmdb(200);

        assertThat(result).isNotNull();
        assertThat(result.getTmdbId()).isEqualTo(200);
        verify(contentRepository).findByTmdbId(200);
        verify(restTemplate, never()).getForObject(anyString(), any());
    }

    @Test
    void syncTvShowFromTmdb_shouldFetchFromTmdb_whenNotFound() {
        when(contentRepository.findByTmdbId(200)).thenReturn(Optional.empty());

        TmdbTvDetails tvDetails = buildTmdbTvDetails(200, "Test TV Show");
        when(restTemplate.getForObject(anyString(), eq(TmdbTvDetails.class)))
                .thenReturn(tvDetails);

        Content savedContent = buildContent(1L, 200, Content.ContentType.TV);
        when(contentRepository.save(any(Content.class))).thenReturn(savedContent);

        Content result = tmdbService.syncTvShowFromTmdb(200);

        assertThat(result).isNotNull();
        verify(contentRepository).findByTmdbId(200);
        verify(restTemplate).getForObject(anyString(), eq(TmdbTvDetails.class));
        verify(contentRepository).save(any(Content.class));
    }

    @Test
    void syncTvShowFromTmdb_shouldThrow_whenTmdbApiError() {
        when(contentRepository.findByTmdbId(200)).thenReturn(Optional.empty());
        when(restTemplate.getForObject(anyString(), eq(TmdbTvDetails.class)))
                .thenThrow(new RuntimeException("TMDB API Error"));

        assertThatThrownBy(() -> tmdbService.syncTvShowFromTmdb(200))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se pudo obtener la serie de TMDB");
    }

    // ---------- searchMovies ----------

    @Test
    void searchMovies_shouldReturnResults_whenFound() {
        TmdbSearchResponse response = buildSearchResponse(
                buildMovieResult(100, "Movie 1"),
                buildMovieResult(101, "Movie 2")
        );

        when(restTemplate.getForObject(anyString(), eq(TmdbSearchResponse.class)))
                .thenReturn(response);

        List<Content> results = tmdbService.searchMovies("test", 1);

        assertThat(results).hasSize(2);
        assertThat(results)
                .extracting(Content::getTitle)
                .containsExactly("Movie 1", "Movie 2");
        verify(restTemplate).getForObject(anyString(), eq(TmdbSearchResponse.class));
    }

    @Test
    void searchMovies_shouldReturnEmpty_whenNoResults() {
        TmdbSearchResponse response = new TmdbSearchResponse();
        response.setResults(Collections.emptyList());

        when(restTemplate.getForObject(anyString(), eq(TmdbSearchResponse.class)))
                .thenReturn(response);

        List<Content> results = tmdbService.searchMovies("xyz", 1);

        assertThat(results).isEmpty();
    }

    @Test
    void searchMovies_shouldReturnEmpty_whenApiError() {
        when(restTemplate.getForObject(anyString(), eq(TmdbSearchResponse.class)))
                .thenThrow(new RuntimeException("API Error"));

        List<Content> results = tmdbService.searchMovies("test", 1);

        assertThat(results).isEmpty();
    }

    @Test
    void searchMovies_shouldUseDefaultPage_whenPageIsNull() {
        TmdbSearchResponse response = buildSearchResponse();
        when(restTemplate.getForObject(anyString(), eq(TmdbSearchResponse.class)))
                .thenReturn(response);

        tmdbService.searchMovies("test", null);

        verify(restTemplate).getForObject(contains("page=1"), eq(TmdbSearchResponse.class));
    }

    // ---------- getPopularMovies ----------

    @Test
    void getPopularMovies_shouldReturnResults() {
        TmdbSearchResponse response = buildSearchResponse(
                buildMovieResult(100, "Popular Movie 1"),
                buildMovieResult(101, "Popular Movie 2")
        );

        when(restTemplate.getForObject(anyString(), eq(TmdbSearchResponse.class)))
                .thenReturn(response);

        List<Content> results = tmdbService.getPopularMovies(1);

        assertThat(results).hasSize(2);
        verify(restTemplate).getForObject(contains("/movie/popular"), eq(TmdbSearchResponse.class));
    }

    @Test
    void getPopularMovies_shouldReturnEmpty_whenApiError() {
        when(restTemplate.getForObject(anyString(), eq(TmdbSearchResponse.class)))
                .thenThrow(new RuntimeException("API Error"));

        List<Content> results = tmdbService.getPopularMovies(1);

        assertThat(results).isEmpty();
    }

    // ---------- searchTvShows ----------

    @Test
    void searchTvShows_shouldReturnResults_whenFound() {
        TmdbSearchResponse response = buildSearchResponse(
                buildTvResult(200, "TV Show 1"),
                buildTvResult(201, "TV Show 2")
        );

        when(restTemplate.getForObject(anyString(), eq(TmdbSearchResponse.class)))
                .thenReturn(response);

        List<Content> results = tmdbService.searchTvShows("test", 1);

        assertThat(results).hasSize(2);
        assertThat(results)
                .extracting(Content::getContentType)
                .containsOnly(Content.ContentType.TV);
        verify(restTemplate).getForObject(anyString(), eq(TmdbSearchResponse.class));
    }

    @Test
    void searchTvShows_shouldReturnEmpty_whenNoResults() {
        TmdbSearchResponse response = new TmdbSearchResponse();
        response.setResults(Collections.emptyList());

        when(restTemplate.getForObject(anyString(), eq(TmdbSearchResponse.class)))
                .thenReturn(response);

        List<Content> results = tmdbService.searchTvShows("xyz", 1);

        assertThat(results).isEmpty();
    }

    @Test
    void searchTvShows_shouldReturnEmpty_whenApiError() {
        when(restTemplate.getForObject(anyString(), eq(TmdbSearchResponse.class)))
                .thenThrow(new RuntimeException("API Error"));

        List<Content> results = tmdbService.searchTvShows("test", 1);

        assertThat(results).isEmpty();
    }

    // ---------- getPopularTvShows ----------

    @Test
    void getPopularTvShows_shouldReturnResults() {
        TmdbSearchResponse response = buildSearchResponse(
                buildTvResult(200, "Popular TV 1"),
                buildTvResult(201, "Popular TV 2")
        );

        when(restTemplate.getForObject(anyString(), eq(TmdbSearchResponse.class)))
                .thenReturn(response);

        List<Content> results = tmdbService.getPopularTvShows(1);

        assertThat(results).hasSize(2);
        verify(restTemplate).getForObject(contains("/tv/popular"), eq(TmdbSearchResponse.class));
    }

    @Test
    void getPopularTvShows_shouldReturnEmpty_whenApiError() {
        when(restTemplate.getForObject(anyString(), eq(TmdbSearchResponse.class)))
                .thenThrow(new RuntimeException("API Error"));

        List<Content> results = tmdbService.getPopularTvShows(1);

        assertThat(results).isEmpty();
    }

    // ---------- helpers ----------

    private Content buildContent(Long id, Integer tmdbId, Content.ContentType type) {
        Content c = new Content();
        c.setId(id);
        c.setTmdbId(tmdbId);
        c.setTitle("Test Content");
        c.setContentType(type);
        return c;
    }

    private TmdbMovieDetails buildTmdbMovieDetails(Integer id, String title) {
        TmdbMovieDetails details = new TmdbMovieDetails();
        details.setId(id);
        details.setTitle(title);
        details.setOverview("Overview");
        details.setRelease_date("2020-01-01");
        details.setPoster_path("/poster.jpg");
        details.setBackdrop_path("/backdrop.jpg");
        details.setVote_average(8.5);
        details.setVote_count(1000);
        
        TmdbMovieDetails.Genre genre = new TmdbMovieDetails.Genre();
        genre.setName("Action");
        details.setGenres(List.of(genre));
        
        return details;
    }

    private TmdbTvDetails buildTmdbTvDetails(Integer id, String name) {
        TmdbTvDetails details = new TmdbTvDetails();
        details.setId(id);
        details.setName(name);
        details.setOverview("Overview");
        details.setFirst_air_date("2020-01-01");
        details.setPoster_path("/poster.jpg");
        details.setBackdrop_path("/backdrop.jpg");
        details.setVote_average(8.5);
        details.setVote_count(1000);
        
        TmdbTvDetails.Genre genre = new TmdbTvDetails.Genre();
        genre.setName("Drama");
        details.setGenres(List.of(genre));
        
        return details;
    }

    private TmdbSearchResponse buildSearchResponse(TmdbSearchResponse.TmdbMovieResult... results) {
        TmdbSearchResponse response = new TmdbSearchResponse();
        response.setResults(List.of(results));
        return response;
    }

    private TmdbSearchResponse.TmdbMovieResult buildMovieResult(Integer id, String title) {
        TmdbSearchResponse.TmdbMovieResult result = new TmdbSearchResponse.TmdbMovieResult();
        result.setId(id);
        result.setTitle(title);
        result.setOverview("Overview");
        result.setPosterPath("/poster.jpg");
        result.setReleaseDate("2020-01-01");
        result.setVoteAverage(8.0);
        result.setVoteCount(500);
        
        TmdbSearchResponse.TmdbMovieResult.Genre genre = new TmdbSearchResponse.TmdbMovieResult.Genre();
        genre.setName("Action");
        result.setGenres(List.of(genre));
        
        return result;
    }

    private TmdbSearchResponse.TmdbMovieResult buildTvResult(Integer id, String name) {
        TmdbSearchResponse.TmdbMovieResult result = new TmdbSearchResponse.TmdbMovieResult();
        result.setId(id);
        result.setName(name);
        result.setOverview("Overview");
        result.setPosterPath("/poster.jpg");
        result.setFirstAirDate("2020-01-01");
        result.setVoteAverage(8.0);
        result.setVoteCount(500);
        
        TmdbSearchResponse.TmdbMovieResult.Genre genre = new TmdbSearchResponse.TmdbMovieResult.Genre();
        genre.setName("Drama");
        result.setGenres(List.of(genre));
        
        return result;
    }
}