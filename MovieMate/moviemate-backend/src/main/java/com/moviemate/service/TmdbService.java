package com.moviemate.service;

import com.moviemate.dto.CastMemberDto;
import com.moviemate.dto.EpisodeDto;
import com.moviemate.dto.GenreDto;
import com.moviemate.dto.PersonDto;
import com.moviemate.dto.SeasonDto;
import com.moviemate.dto.SeasonSummaryDto;
import com.moviemate.dto.WatchProvidersDto;
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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    // ── Géneros ────────────────────────────────────────────────────

    public List<GenreDto> getMovieGenres() {
        return fetchGenres("/genre/movie/list");
    }

    public List<GenreDto> getTvGenres() {
        return fetchGenres("/genre/tv/list");
    }

    private List<GenreDto> fetchGenres(String path) {
        String url = buildTmdbUrl(path).build().toUriString();
        try {
            GenreListResponse response = restTemplate.getForObject(url, GenreListResponse.class);
            if (response != null && response.getGenres() != null) {
                return response.getGenres().stream()
                        .map(g -> new GenreDto(g.getId(), g.getName()))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("Error fetching genres from {}: {}", path, e.getMessage());
        }
        return Collections.emptyList();
    }

    // ── Discover ───────────────────────────────────────────────────

    public List<Content> discoverMovies(Integer genreId, Integer year, Double minRating, String sortBy, Integer page) {
        UriComponentsBuilder builder = buildTmdbUrl("/discover/movie")
                .queryParam("page", page != null ? page : 1)
                .queryParam("vote_count.gte", 20);

        if (genreId != null) builder = builder.queryParam("with_genres", genreId);
        if (year != null) builder = builder.queryParam("primary_release_year", year);
        if (minRating != null) builder = builder.queryParam("vote_average.gte", minRating);
        if (sortBy != null && !sortBy.isBlank()) builder = builder.queryParam("sort_by", sortBy);

        return fetchAndMapContent(builder.build().toUriString(), Content.ContentType.MOVIE);
    }

    public List<Content> discoverTvShows(Integer genreId, Integer year, Double minRating, String sortBy, Integer page) {
        UriComponentsBuilder builder = buildTmdbUrl("/discover/tv")
                .queryParam("page", page != null ? page : 1)
                .queryParam("vote_count.gte", 20);

        if (genreId != null) builder = builder.queryParam("with_genres", genreId);
        if (year != null) builder = builder.queryParam("first_air_date_year", year);
        if (minRating != null) builder = builder.queryParam("vote_average.gte", minRating);
        if (sortBy != null && !sortBy.isBlank()) builder = builder.queryParam("sort_by", sortBy);

        return fetchAndMapContent(builder.build().toUriString(), Content.ContentType.TV);
    }

    // ── ¿Dónde ver? ────────────────────────────────────────────────

    /**
     * Obtiene los proveedores de streaming, alquiler y compra para un contenido.
     * Prioriza ES (España), luego US; si ninguno existe usa el primer país disponible.
     */
    public WatchProvidersDto getWatchProviders(Integer tmdbId, String contentType) {
        String path = "TV".equalsIgnoreCase(contentType)
                ? "/tv/" + tmdbId + "/watch/providers"
                : "/movie/" + tmdbId + "/watch/providers";

        String url = buildTmdbUrl(path).build().toUriString();
        try {
            TmdbProvidersResponse response = restTemplate.getForObject(url, TmdbProvidersResponse.class);
            if (response != null && response.getResults() != null && !response.getResults().isEmpty()) {
                TmdbProvidersResponse.CountryProviders country =
                        response.getResults().getOrDefault("ES",
                        response.getResults().getOrDefault("US",
                        response.getResults().values().iterator().next()));

                if (country != null) {
                    WatchProvidersDto dto = new WatchProvidersDto();
                    dto.setLink(country.getLink());
                    dto.setFlatrate(mapProviders(country.getFlatrate()));
                    dto.setRent(mapProviders(country.getRent()));
                    dto.setBuy(mapProviders(country.getBuy()));
                    return dto;
                }
            }
        } catch (Exception e) {
            log.error("Error fetching watch providers for tmdbId={}: {}", tmdbId, e.getMessage());
        }
        return new WatchProvidersDto();
    }

    private List<WatchProvidersDto.ProviderDto> mapProviders(List<TmdbProvidersResponse.Provider> providers) {
        if (providers == null) return Collections.emptyList();
        return providers.stream().map(p -> {
            WatchProvidersDto.ProviderDto dto = new WatchProvidersDto.ProviderDto();
            dto.setProviderId(p.getProviderId());
            dto.setProviderName(p.getProviderName());
            if (p.getLogoPath() != null) {
                dto.setLogoUrl(imageBaseUrl + "/w92" + p.getLogoPath());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    // ── Personas ───────────────────────────────────────────────────

    public PersonDto getPersonDetails(Integer personId) {
        String url = buildTmdbUrl("/person/" + personId).build().toUriString();
        try {
            TmdbPersonDetails p = restTemplate.getForObject(url, TmdbPersonDetails.class);
            if (p != null) {
                PersonDto dto = new PersonDto();
                dto.setId(p.getId());
                dto.setName(p.getName());
                dto.setBiography(p.getBiography());
                dto.setBirthday(p.getBirthday());
                dto.setDeathday(p.getDeathday());
                dto.setPlaceOfBirth(p.getPlace_of_birth());
                dto.setKnownForDepartment(p.getKnown_for_department());
                if (p.getProfile_path() != null) {
                    dto.setProfileUrl(imageBaseUrl + "/w342" + p.getProfile_path());
                }
                return dto;
            }
        } catch (Exception e) {
            log.error("Error fetching person {}: {}", personId, e.getMessage());
        }
        return null;
    }

    public List<Content> getPersonCredits(Integer personId) {
        String url = buildTmdbUrl("/person/" + personId + "/combined_credits").build().toUriString();
        try {
            TmdbCombinedCredits credits = restTemplate.getForObject(url, TmdbCombinedCredits.class);
            if (credits != null && credits.getCast() != null) {
                return credits.getCast().stream()
                        .filter(c -> "movie".equals(c.getMedia_type()) || "tv".equals(c.getMedia_type()))
                        .sorted((a, b) -> Double.compare(
                                b.getPopularity() != null ? b.getPopularity() : 0,
                                a.getPopularity() != null ? a.getPopularity() : 0))
                        .limit(30)
                        .map(c -> {
                            Content.ContentType type = "tv".equals(c.getMedia_type())
                                    ? Content.ContentType.TV : Content.ContentType.MOVIE;
                            TmdbSearchResponse.TmdbMovieResult r = new TmdbSearchResponse.TmdbMovieResult();
                            r.setId(c.getId());
                            r.setTitle(c.getTitle());
                            r.setName(c.getName());
                            r.setOverview(c.getOverview());
                            r.setPosterPath(c.getPoster_path());
                            r.setBackdropPath(c.getBackdrop_path());
                            r.setReleaseDate(c.getRelease_date());
                            r.setFirstAirDate(c.getFirst_air_date());
                            r.setVoteAverage(c.getVote_average());
                            r.setVoteCount(c.getVote_count());
                            r.setMediaType(c.getMedia_type());
                            return mapSearchResultToContent(r, type);
                        })
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("Error fetching credits for person {}: {}", personId, e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<CastMemberDto> getContentCredits(Integer tmdbId, String contentType) {
        String path = "TV".equalsIgnoreCase(contentType)
                ? "/tv/" + tmdbId + "/credits"
                : "/movie/" + tmdbId + "/credits";
        String url = buildTmdbUrl(path).build().toUriString();

        try {
            TmdbCreditsResponse credits = restTemplate.getForObject(url, TmdbCreditsResponse.class);
            List<CastMemberDto> result = new java.util.ArrayList<>();

            // Top 10 actores
            if (credits != null && credits.getCast() != null) {
                credits.getCast().stream().limit(10).forEach(c -> {
                    CastMemberDto dto = new CastMemberDto();
                    dto.setPersonId(c.getId());
                    dto.setName(c.getName());
                    dto.setCharacter(c.getCharacter());
                    dto.setDepartment("Acting");
                    if (c.getProfile_path() != null) {
                        dto.setProfileUrl(imageBaseUrl + "/w185" + c.getProfile_path());
                    }
                    result.add(dto);
                });
            }

            // Director(es)
            if (credits != null && credits.getCrew() != null) {
                credits.getCrew().stream()
                        .filter(c -> "Director".equals(c.getJob()))
                        .forEach(c -> {
                            CastMemberDto dto = new CastMemberDto();
                            dto.setPersonId(c.getId());
                            dto.setName(c.getName());
                            dto.setJob(c.getJob());
                            dto.setDepartment(c.getDepartment());
                            if (c.getProfile_path() != null) {
                                dto.setProfileUrl(imageBaseUrl + "/w185" + c.getProfile_path());
                            }
                            result.add(0, dto); // Director va primero
                        });
            }
            return result;
        } catch (Exception e) {
            log.error("Error fetching credits for content {}: {}", tmdbId, e.getMessage());
        }
        return Collections.emptyList();
    }

    // ── Temporadas y episodios ──────────────────────────────────────

    public List<SeasonSummaryDto> getTvSeasonsSummary(Integer tmdbId) {
        String url = buildTmdbUrl("/tv/" + tmdbId).build().toUriString();
        try {
            TmdbTvSeriesDetails details = restTemplate.getForObject(url, TmdbTvSeriesDetails.class);
            if (details == null || details.getSeasons() == null) return Collections.emptyList();
            return details.getSeasons().stream()
                    .filter(s -> s.getSeasonNumber() != null && s.getSeasonNumber() > 0)
                    .map(s -> {
                        SeasonSummaryDto dto = new SeasonSummaryDto();
                        dto.setSeasonNumber(s.getSeasonNumber());
                        dto.setName(s.getName());
                        dto.setOverview(s.getOverview());
                        dto.setEpisodeCount(s.getEpisodeCount());
                        dto.setAirDate(s.getAirDate());
                        if (s.getPosterPath() != null) {
                            dto.setPosterUrl(imageBaseUrl + "/w185" + s.getPosterPath());
                        }
                        return dto;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching seasons for tv {}: {}", tmdbId, e.getMessage());
        }
        return Collections.emptyList();
    }

    public SeasonDto getSeasonDetails(Integer tmdbId, Integer seasonNumber) {
        String url = buildTmdbUrl("/tv/" + tmdbId + "/season/" + seasonNumber).build().toUriString();
        try {
            TmdbSeasonDetails tmdb = restTemplate.getForObject(url, TmdbSeasonDetails.class);
            if (tmdb == null) return null;
            SeasonDto dto = new SeasonDto();
            dto.setSeasonNumber(tmdb.getSeasonNumber());
            dto.setName(tmdb.getName());
            dto.setOverview(tmdb.getOverview());
            if (tmdb.getPosterPath() != null) {
                dto.setPosterUrl(imageBaseUrl + "/w185" + tmdb.getPosterPath());
            }
            if (tmdb.getEpisodes() != null) {
                dto.setEpisodeCount(tmdb.getEpisodes().size());
                dto.setEpisodes(tmdb.getEpisodes().stream().map(ep -> {
                    EpisodeDto epDto = new EpisodeDto();
                    epDto.setEpisodeNumber(ep.getEpisodeNumber());
                    epDto.setName(ep.getName());
                    epDto.setOverview(ep.getOverview());
                    epDto.setAirDate(ep.getAirDate());
                    epDto.setRuntime(ep.getRuntime());
                    epDto.setVoteAverage(ep.getVoteAverage());
                    if (ep.getStillPath() != null) {
                        epDto.setStillUrl(imageBaseUrl + "/w300" + ep.getStillPath());
                    }
                    return epDto;
                }).collect(Collectors.toList()));
            }
            return dto;
        } catch (Exception e) {
            log.error("Error fetching season {}/{}: {}", tmdbId, seasonNumber, e.getMessage());
        }
        return null;
    }

    // ── DTOs internos para temporadas ──────────────────────────────

    @Data
    private static class TmdbTvSeriesDetails {
        private List<TmdbSeasonSummary> seasons;

        @Data
        static class TmdbSeasonSummary {
            @JsonProperty("season_number") private Integer seasonNumber;
            @JsonProperty("name")          private String name;
            @JsonProperty("overview")      private String overview;
            @JsonProperty("episode_count") private Integer episodeCount;
            @JsonProperty("poster_path")   private String posterPath;
            @JsonProperty("air_date")      private String airDate;
        }
    }

    @Data
    private static class TmdbSeasonDetails {
        @JsonProperty("season_number") private Integer seasonNumber;
        @JsonProperty("name")          private String name;
        @JsonProperty("overview")      private String overview;
        @JsonProperty("poster_path")   private String posterPath;
        @JsonProperty("episodes")      private List<TmdbEpisode> episodes;

        @Data
        static class TmdbEpisode {
            @JsonProperty("episode_number") private Integer episodeNumber;
            @JsonProperty("name")           private String name;
            @JsonProperty("overview")       private String overview;
            @JsonProperty("air_date")       private String airDate;
            @JsonProperty("runtime")        private Integer runtime;
            @JsonProperty("still_path")     private String stillPath;
            @JsonProperty("vote_average")   private Double voteAverage;
        }
    }

    // ── DTOs internos para TMDB persons/credits ────────────────────

    @Data
    private static class TmdbPersonDetails {
        private Integer id;
        private String name;
        private String biography;
        private String birthday;
        private String deathday;
        private String profile_path;
        private String place_of_birth;
        private String known_for_department;
    }

    @Data
    private static class TmdbCombinedCredits {
        private List<CreditItem> cast;

        @Data
        static class CreditItem {
            private Integer id;
            private String title;      // movie
            private String name;       // tv
            private String overview;
            private String poster_path;
            private String backdrop_path;
            private String release_date;
            private String first_air_date;
            private String media_type;
            private Double vote_average;
            private Integer vote_count;
            private Double popularity;
        }
    }

    @Data
    private static class TmdbCreditsResponse {
        private List<CastEntry> cast;
        private List<CrewEntry> crew;

        @Data
        static class CastEntry {
            private Integer id;
            private String name;
            private String character;
            private String profile_path;
        }

        @Data
        static class CrewEntry {
            private Integer id;
            private String name;
            private String job;
            private String department;
            private String profile_path;
        }
    }

    // ── DTO interno para respuesta de providers de TMDB ────────────

    @Data
    private static class TmdbProvidersResponse {
        private Map<String, CountryProviders> results;

        @Data
        static class CountryProviders {
            private String link;
            private List<Provider> flatrate;
            private List<Provider> rent;
            private List<Provider> buy;
        }

        @Data
        static class Provider {
            @JsonProperty("provider_id")   private Integer providerId;
            @JsonProperty("provider_name") private String providerName;
            @JsonProperty("logo_path")     private String logoPath;
        }
    }

    // ── DTO interno para deserializar la respuesta de géneros de TMDB ─

    @Data
    private static class GenreListResponse {
        private List<TmdbGenre> genres;

        @Data
        static class TmdbGenre {
            private Integer id;
            private String name;
        }
    }

    private UriComponentsBuilder buildTmdbUrl(String path) {
        return UriComponentsBuilder.fromUriString(baseUrl + path)
                .queryParam("api_key", apiKey)
                .queryParam("language", language);
    }

}