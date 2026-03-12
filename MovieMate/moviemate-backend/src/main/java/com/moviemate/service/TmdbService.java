package com.moviemate.service;

import com.moviemate.dto.tmdb.MultiSearchResult;
import com.moviemate.dto.tmdb.TmdbMovieDetails;
import com.moviemate.dto.tmdb.TmdbSearchResponse;
import com.moviemate.dto.tmdb.TmdbTvDetails;
import com.moviemate.entity.Content;
import com.moviemate.repository.ContentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TmdbService {

    private final RestTemplate restTemplate;
    private final ContentRepository contentRepository;

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Value("${tmdb.api.base-url}")
    private String baseUrl;

    @Value("${tmdb.api.image-base-url}")
    private String imageBaseUrl;

    @Value("${tmdb.api.language}")
    private String language;

    public Content syncMovieFromTmdb(Integer tmdbId) {
        return contentRepository.findByTmdbId(tmdbId)
                .orElseGet(() -> fetchAndSaveMovie(tmdbId));
    }

    public Content syncTvShowFromTmdb(Integer tmdbId) {
        return contentRepository.findByTmdbId(tmdbId)
                .orElseGet(() -> fetchAndSaveTvShow(tmdbId));
    }

    private Content fetchAndSaveMovie(Integer tmdbId) {
        String url = buildTmdbUrl("/movie/" + tmdbId).build().toUriString();
        
        try {
            TmdbMovieDetails response = restTemplate.getForObject(url, TmdbMovieDetails.class);
            
            if (response != null) {
                return mapMovieToContent(response);
            }
        } catch (Exception e) {
            log.error("Error fetching movie from TMDB: {}", e.getMessage());
            throw new RuntimeException("No se pudo obtener la película de TMDB");
        }
        
        return null;
    }

    private Content fetchAndSaveTvShow(Integer tmdbId) {
        String url = buildTmdbUrl("/tv/" + tmdbId).build().toUriString();
        
        try {
            TmdbTvDetails response = restTemplate.getForObject(url, TmdbTvDetails.class);
            
            if (response != null) {
                return mapTvToContent(response);
            }
        } catch (Exception e) {
            log.error("Error fetching TV show from TMDB: {}", e.getMessage());
            throw new RuntimeException("No se pudo obtener la serie de TMDB");
        }
        
        return null;
    }

    private Content mapMovieToContent(TmdbMovieDetails response) {
        Content content = new Content();
        content.setTmdbId(response.getId());
        content.setTitle(response.getTitle());
        content.setContentType(Content.ContentType.MOVIE);
        content.setSynopsis(response.getOverview());

        if (response.getRelease_date() != null && !response.getRelease_date().isEmpty()) {
            try {
                content.setReleaseDate(LocalDate.parse(response.getRelease_date()));
            } catch (Exception ignored) {}
        }

        if (response.getPoster_path() != null) {
            content.setPosterUrl(imageBaseUrl + "/w500" + response.getPoster_path());
        }
        if (response.getBackdrop_path() != null) {
            content.setBackdropUrl(imageBaseUrl + "/w1280" + response.getBackdrop_path());
        }

        if (response.getGenres() != null) {
            List<String> genres = response.getGenres().stream()
                    .map(TmdbMovieDetails.Genre::getName)
                    .toList();
            content.setGenres(genres);
        }

        if (response.getVote_average() != null) {
            content.setTmdbRating(response.getVote_average());
        }

        content.setTmdbVoteCount(response.getVote_count());
        content.setLastTmdbSync(LocalDate.now().atStartOfDay());
        return contentRepository.save(content);
    }

    private Content mapTvToContent(TmdbTvDetails response) {
        Content content = new Content();
        content.setTmdbId(response.getId());
        content.setTitle(response.getName()); 
        content.setContentType(Content.ContentType.TV);
        content.setSynopsis(response.getOverview());

        if (response.getFirst_air_date() != null && !response.getFirst_air_date().isEmpty()) {
            try {
                content.setReleaseDate(LocalDate.parse(response.getFirst_air_date()));
            } catch (Exception ignored) {}
        }

        if (response.getPoster_path() != null) {
            content.setPosterUrl(imageBaseUrl + "/w500" + response.getPoster_path());
        }
        if (response.getBackdrop_path() != null) {
            content.setBackdropUrl(imageBaseUrl + "/w1280" + response.getBackdrop_path());
        }

        if (response.getGenres() != null) {
            List<String> genres = response.getGenres().stream()
                    .map(TmdbTvDetails.Genre::getName)
                    .toList();
            content.setGenres(genres);
        }

        if (response.getVote_average() != null) {
            content.setTmdbRating(response.getVote_average());
        }

        content.setTmdbVoteCount(response.getVote_count());
        content.setLastTmdbSync(LocalDate.now().atStartOfDay());
        return contentRepository.save(content);
    }

    
    public MultiSearchResult detectContentType(Integer tmdbId) {
        UriComponentsBuilder builder = buildTmdbUrl("/search/multi")
            .queryParam("query", tmdbId)
            .queryParam("language", language)
            .queryParam("page", 1);
        
        String url = builder.build().toUriString();
        
        try {
            return restTemplate.getForObject(url, MultiSearchResult.class);
        } catch (Exception e) {
            log.error("Error detectando tipo TMDB {}: {}", tmdbId, e.getMessage());
            throw new RuntimeException("No se pudo detectar tipo de contenido");
        }
    }


    public List<Content> searchMovies(String query, Integer page) {
        String url = buildTmdbUrl("/search/movie")
                .queryParam("query", query)
                .queryParam("page", page != null ? page : 1)
                .build()
                .toUriString();

        try {
            TmdbSearchResponse response = restTemplate.getForObject(url, TmdbSearchResponse.class);

            if (response != null && response.getResults() != null) {
                return response.getResults().stream()
                        .map(result -> mapSearchResultToContent(result, Content.ContentType.MOVIE))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("Error searching movies: {}", e.getMessage());
        }

        return Collections.emptyList();
    }


    public List<Content> getPopularMovies(Integer page) {
        String url = buildTmdbUrl("/movie/popular")
                .queryParam("page", page != null ? page : 1)
                .build()
                .toUriString();
        
        return fetchAndMapContent(url, Content.ContentType.MOVIE);
    }

    public List<Content> searchTvShows(String query, Integer page) {
        String url = buildTmdbUrl("/search/tv")
                .queryParam("query", query)
                .queryParam("page", page != null ? page : 1)
                .build()
                .toUriString();

        try {
            TmdbSearchResponse response = restTemplate.getForObject(url, TmdbSearchResponse.class);

            if (response != null && response.getResults() != null) {
                return response.getResults().stream()
                        .map(result -> mapSearchResultToContent(result, Content.ContentType.TV))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("Error searching TV shows: {}", e.getMessage());
        }

        return Collections.emptyList();
    }

    public List<Content> getPopularTvShows(Integer page) {
        String url = buildTmdbUrl("/tv/popular")
                .queryParam("page", page != null ? page : 1)
                .build()
                .toUriString();

        return fetchAndMapContent(url, Content.ContentType.TV);
    }

    public List<Content> getTrendingAll(Integer page) {
        String url = buildTmdbUrl("/trending/all/week")
                .queryParam("page", page != null ? page : 1)
                .build()
                .toUriString();

        try {
            TmdbSearchResponse response = restTemplate.getForObject(url, TmdbSearchResponse.class);
            if (response != null && response.getResults() != null) {
                return response.getResults().stream()
                        .filter(r -> "movie".equals(r.getMediaType()) || "tv".equals(r.getMediaType()))
                        .map(r -> {
                            Content.ContentType type = "tv".equals(r.getMediaType())
                                    ? Content.ContentType.TV
                                    : Content.ContentType.MOVIE;
                            return mapSearchResultToContent(r, type);
                        })
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("Error fetching trending content: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    private List<Content> fetchAndMapContent(String url, Content.ContentType contentType) {
        try {
            TmdbSearchResponse response = restTemplate.getForObject(url, TmdbSearchResponse.class);
            
            if (response != null && response.getResults() != null) {
                return response.getResults().stream()
                        .map(result -> mapSearchResultToContent(result, contentType))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("Error fetching popular content: {}", e.getMessage());
        }
        
        return Collections.emptyList();
    }

    private Content mapSearchResultToContent(TmdbSearchResponse.TmdbMovieResult result, Content.ContentType contentType) {
        Content content = new Content();
        content.setTmdbId(result.getId());

        String title = result.getTitle();
        if (title == null || title.isBlank()) {
            title = result.getName(); // para TV
        }

        content.setTitle(title);
        content.setContentType(contentType);
        content.setSynopsis(result.getOverview());
        
        if (result.getPosterPath() != null) {
            content.setPosterUrl(imageBaseUrl + "/w500" + result.getPosterPath());
        }
        if (result.getBackdropPath() != null) {
            content.setBackdropUrl(imageBaseUrl + "/w1280" + result.getBackdropPath());
        }

        String date = result.getReleaseDate();
        if (date == null || date.isBlank()) {
            date = result.getFirstAirDate(); // para TV
        }
        if (date != null && !date.isEmpty()) {
            try {
                content.setReleaseDate(LocalDate.parse(date));
            } catch (Exception ignored) {}
        }

        content.setGenres(result.getGenres() != null ? result.getGenres().stream() 
            .map(TmdbSearchResponse.TmdbMovieResult.Genre::getName) 
            .toList() : Collections.emptyList()
        ); 
        
        if (result.getVoteAverage() != null) {
            content.setTmdbRating(result.getVoteAverage());
        }

        content.setTmdbVoteCount(result.getVoteCount());
        content.setLastTmdbSync(LocalDate.now().atStartOfDay());
        
        return content;
    }

    private UriComponentsBuilder buildTmdbUrl(String path) {
        return UriComponentsBuilder.fromUriString(baseUrl + path)
                .queryParam("api_key", apiKey)
                .queryParam("language", language);
    }

}