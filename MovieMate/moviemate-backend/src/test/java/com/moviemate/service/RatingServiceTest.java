package com.moviemate.service;

import com.moviemate.dto.ContentResponse;
import com.moviemate.dto.RatingRequest;
import com.moviemate.dto.RatingResponse;
import com.moviemate.dto.UserResponse;
import com.moviemate.entity.Content;
import com.moviemate.entity.Rating;
import com.moviemate.entity.User;
import com.moviemate.repository.ContentRepository;
import com.moviemate.repository.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class RatingServiceTest {

    private RatingRepository ratingRepository;
    private ContentRepository contentRepository;
    private ContentService contentService;
    private RatingService ratingService;

    @BeforeEach
    void setUp() {
        ratingRepository = mock(RatingRepository.class);
        contentRepository = mock(ContentRepository.class);
        contentService = mock(ContentService.class);
        ratingService = new RatingService(ratingRepository, contentRepository, contentService);
    }

    // ---------- createOrUpdateRating ----------

    @Test
    void createOrUpdateRating_shouldCreateNew_whenNoExistingRating() {
        User user = buildUser(1L, "chris");
        RatingRequest request = buildRatingRequest();

        Content content = buildContent();
        when(contentService.getOrFetch(request.getTmdbId()))
                .thenReturn(content);
        when(ratingRepository.findByUserAndContent(user, content))
                .thenReturn(Optional.empty());

        Rating saved = new Rating();
        saved.setId(10L);
        saved.setUser(user);
        saved.setContent(content);
        saved.setRating(request.getRating());
        saved.setReviewText(request.getReviewText());
        saved.setEmotionalTag(request.getEmotionalTag());
        saved.setStatus(request.getStatus());
        saved.setWatchedDate(request.getWatchedDate());
        saved.setCreatedAt(LocalDateTime.now());

        when(ratingRepository.save(any(Rating.class))).thenReturn(saved);
        when(ratingRepository.calculateAverageRatingByContent(content.getId()))
                .thenReturn(8.0);
        when(ratingRepository.countRatingsByContent(content.getId()))
                .thenReturn(5);

        when(contentRepository.save(any(Content.class)))
            .thenAnswer(i -> i.getArgument(0));

        RatingResponse response = ratingService.createOrUpdateRating(user, request);

        // verifica mapeo principal
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getRating()).isEqualTo(request.getRating());
        assertThat(response.getReviewText()).isEqualTo(request.getReviewText());
        assertThat(response.getUser().getUsername()).isEqualTo("chris");
        assertThat(response.getContent().getId()).isEqualTo(content.getId());

        assertThat(content.getAppRating()).isEqualTo(8.0);
        assertThat(content.getAppVoteCount()).isEqualTo(5);

        // verifica actualización de estadísticas
        verify(ratingRepository).calculateAverageRatingByContent(content.getId());
        verify(ratingRepository).countRatingsByContent(content.getId());
        verify(contentRepository, times(1)).save(content);
    }

    @Test
    void createOrUpdateRating_shouldUpdateExisting_whenFound() {
        User user = buildUser(1L, "chris");
        RatingRequest request = buildRatingRequest();

        Content content = buildContent();
        when(contentService.getOrFetch(request.getTmdbId()))
                .thenReturn(content);

        Rating existing = new Rating();
        existing.setId(10L);
        existing.setUser(user);
        existing.setContent(content);
        existing.setRating(5);

        when(ratingRepository.findByUserAndContent(user, content))
                .thenReturn(Optional.of(existing));

        when(ratingRepository.save(existing)).thenReturn(existing);
        when(ratingRepository.calculateAverageRatingByContent(content.getId()))
                .thenReturn(9.0);
        when(ratingRepository.countRatingsByContent(content.getId()))
                .thenReturn(3);
        when(contentRepository.save(any(Content.class)))
            .thenAnswer(i -> i.getArgument(0));

        RatingResponse response = ratingService.createOrUpdateRating(user, request);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(existing.getRating()).isEqualTo(request.getRating());
        assertThat(existing.getReviewText()).isEqualTo(request.getReviewText());

        assertThat(content.getAppRating()).isEqualTo(9.0);
        assertThat(content.getAppVoteCount()).isEqualTo(3);

        verify(ratingRepository).save(existing);
        verify(contentRepository, times(1)).save(content);;
    }

    @Test
    void createOrUpdateRating_shouldSetZeroAverage_whenRepositoryReturnsNull() {
        User user = buildUser(1L, "chris");
        RatingRequest request = buildRatingRequest();
        Content content = buildContent();

        when(contentService.getOrFetch(anyInt())).thenReturn(content);
        when(ratingRepository.findByUserAndContent(user, content)).thenReturn(Optional.empty());

        Rating saved = new Rating();
        saved.setId(10L);
        saved.setUser(user);
        saved.setContent(content);
        saved.setRating(request.getRating());
        saved.setCreatedAt(LocalDateTime.now());

        when(ratingRepository.save(any(Rating.class))).thenReturn(saved);
        when(ratingRepository.calculateAverageRatingByContent(content.getId()))
                .thenReturn(null);       // caso especial
        when(ratingRepository.countRatingsByContent(content.getId()))
                .thenReturn(0);
        when(contentRepository.save(any(Content.class)))
            .thenAnswer(i -> i.getArgument(0));

        ratingService.createOrUpdateRating(user, request);

        assertThat(content.getAppRating()).isEqualTo(0.0);
        assertThat(content.getAppVoteCount()).isEqualTo(0);
        verify(contentRepository, times(1)).save(content);;
    }

    // ---------- deleteRating ----------

    @Test
    void deleteRating_shouldDelete_whenUserIsOwner() {
        User user = buildUser(1L, "chris");
        Content content = buildContent();

        Rating rating = new Rating();
        rating.setId(20L);
        rating.setUser(user);
        rating.setContent(content);

        when(ratingRepository.findById(20L)).thenReturn(Optional.of(rating));
        when(ratingRepository.calculateAverageRatingByContent(content.getId()))
                .thenReturn(7.0);
        when(ratingRepository.countRatingsByContent(content.getId()))
                .thenReturn(2);
        when(contentRepository.save(any(Content.class)))
            .thenAnswer(i -> i.getArgument(0));

        ratingService.deleteRating(user, 20L);

        verify(ratingRepository).delete(rating);
        assertThat(content.getAppRating()).isEqualTo(7.0);
        assertThat(content.getAppVoteCount()).isEqualTo(2);
        verify(contentRepository, times(1)).save(content);;
    }

    @Test
    void deleteRating_shouldThrow_whenRatingNotFound() {
        User user = buildUser(1L, "chris");
        when(ratingRepository.findById(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ratingService.deleteRating(user, 20L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Valoración no encontrada");
    }

    @Test
    void deleteRating_shouldThrow_whenUserIsNotOwner() {
        User owner = buildUser(1L, "owner");
        User other = buildUser(2L, "other");
        Content content = buildContent();

        Rating rating = new Rating();
        rating.setId(20L);
        rating.setUser(owner);
        rating.setContent(content);

        when(ratingRepository.findById(20L)).thenReturn(Optional.of(rating));

        assertThatThrownBy(() -> ratingService.deleteRating(other, 20L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No tienes permisos");
                
        verify(ratingRepository, never()).delete(any());
        verify(contentRepository, never()).save(any());
    }

    // ---------- getUserRatings ----------

    @Test
    void getUserRatings_shouldMapAllRatings() {
        User user = buildUser(1L, "chris");
        Content content = buildContent();

        Rating r1 = buildRating(10L, user, content, 4);
        Rating r2 = buildRating(11L, user, content, 3);

        when(ratingRepository.findByUser(user)).thenReturn(List.of(r1, r2));

        List<RatingResponse> responses = ratingService.getUserRatings(user);

        assertThat(responses).hasSize(2);
        assertThat(responses)
                .extracting(RatingResponse::getId)
                .containsExactlyInAnyOrder(10L, 11L);
        assertThat(responses.get(0).getUser().getUsername()).isEqualTo("chris");
    }

    // ---------- getRatingsByContent ----------

    @Test
    void getRatingsByContent_shouldReturnAllRatingsForContentAndUser() {
        User user = buildUser(1L, "chris");
        Content content = buildContent();
        
        Rating r1 = buildRating(10L, user, content, 4);
        Rating r2 = buildRating(11L, user, content, 5);
        
        when(contentRepository.findById(100L)).thenReturn(Optional.of(content));
        when(ratingRepository.findAllByUserAndContent(user, content))
                .thenReturn(List.of(r1, r2));
        
        List<RatingResponse> responses = ratingService.getRatingsByContent(user, 100L);
        
        assertThat(responses).hasSize(2);
        assertThat(responses)
                .extracting(RatingResponse::getId)
                .containsExactlyInAnyOrder(10L, 11L);
    }

    @Test
    void getRatingsByContent_shouldThrow_whenContentNotFound() {
        User user = buildUser(1L, "chris");
        
        when(contentRepository.findById(999L)).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> ratingService.getRatingsByContent(user, 999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Contenido no encontrado");
    }

    // ---------- mapToRatingResponse ----------

    @Test
    void mapToRatingResponse_shouldMapFieldsCorrectly() {
        User user = buildUser(1L, "chris");
        Content content = buildContent();
        Rating rating = buildRating(10L, user, content, 4);

        RatingResponse response = ratingService.mapToRatingResponse(rating);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getRating()).isEqualTo(4);
        assertThat(response.getUser())
                .extracting(UserResponse::getUsername)
                .isEqualTo("chris");
        assertThat(response.getContent())
                .extracting(ContentResponse::getTitle)
                .isEqualTo("Peli");
        assertThat(response.getContent().getReleaseDate())
                .isEqualTo(content.getReleaseDate().toString());
    }

    // ---------- helpers ----------

    private User buildUser(Long id, String username) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setAvatarUrl("avatar.png");
        return u;
    }

    private Content buildContent() {
        Content c = new Content();
        c.setId(100L);
        c.setTmdbId(1000);
        c.setTitle("Peli");
        c.setContentType(Content.ContentType.MOVIE);
        c.setReleaseDate(LocalDate.of(2020, 1, 1));
        c.setPosterUrl("poster.jpg");
        c.setBackdropUrl("backdrop.jpg");
        c.setSynopsis("Sinopsis");
        c.setGenres(new java.util.ArrayList<>(java.util.List.of("Acción", "Aventura")));
        c.setTmdbRating(8.5);
        c.setTmdbVoteCount(1000);
        c.setAppRating(9.0);
        c.setAppVoteCount(100);
        c.setLastTmdbSync(LocalDateTime.now().minusDays(1));
        c.setLastInteraction(LocalDateTime.now().minusHours(5));
        c.setSyncStatus(Content.SyncStatus.FRESH);
        return c;
    }

    private RatingRequest buildRatingRequest() {
        RatingRequest req = new RatingRequest();
        req.setTmdbId(1000);
        req.setContentType(Content.ContentType.MOVIE);
        req.setRating(4);
        req.setReviewText("Muy buena");
        req.setEmotionalTag(Rating.EmotionalTag.INCREIBLE);
        req.setStatus(Rating.Status.VISTA);
        req.setWatchedDate(LocalDate.of(2024, 1, 1));
        return req;
    }

    private Rating buildRating(Long id, User user, Content content, Integer value) {
        Rating r = new Rating();
        r.setId(id);
        r.setUser(user);
        r.setContent(content);
        r.setRating(value);
        r.setReviewText("Texto");
        r.setEmotionalTag(Rating.EmotionalTag.INCREIBLE);
        r.setStatus(Rating.Status.VISTA);
        r.setWatchedDate(LocalDate.of(2024, 1, 1));
        r.setCreatedAt(LocalDateTime.now());
        return r;
    }
}
