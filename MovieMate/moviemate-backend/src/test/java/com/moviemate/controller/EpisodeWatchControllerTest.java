package com.moviemate.controller;

import com.moviemate.dto.SeriesProgressDto;
import com.moviemate.entity.User;
import com.moviemate.security.CustomUserDetails;
import com.moviemate.service.EpisodeWatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class EpisodeWatchControllerTest {

    private EpisodeWatchService episodeWatchService;
    private EpisodeWatchController episodeWatchController;

    @BeforeEach
    void setUp() {
        episodeWatchService = mock(EpisodeWatchService.class);
        episodeWatchController = new EpisodeWatchController(episodeWatchService);
    }

    @Test
    void getSeriesProgress_shouldReturnOk() {
        User user = buildUser(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);
        List<SeriesProgressDto> list = List.of(new SeriesProgressDto(100, "Serie", "p.jpg", 4));
        when(episodeWatchService.getSeriesProgress(user)).thenReturn(list);

        ResponseEntity<List<SeriesProgressDto>> response = episodeWatchController.getSeriesProgress(userDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(list);
        verify(episodeWatchService).getSeriesProgress(user);
    }

    @Test
    void getWatchedEpisodes_shouldReturnOk() {
        User user = buildUser(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Set<String> watched = Set.of("1x01", "1x02");
        when(episodeWatchService.getWatchedEpisodes(user, 100)).thenReturn(watched);

        ResponseEntity<Set<String>> response = episodeWatchController.getWatchedEpisodes(userDetails, 100);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(watched);
        verify(episodeWatchService).getWatchedEpisodes(user, 100);
    }

    @Test
    void toggleEpisodeWatched_shouldReturnOk() {
        User user = buildUser(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);
        when(episodeWatchService.toggleEpisodeWatched(user, 100, 1, 2)).thenReturn(true);

        ResponseEntity<Boolean> response = episodeWatchController.toggleEpisodeWatched(userDetails, 100, 1, 2);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isTrue();
        verify(episodeWatchService).toggleEpisodeWatched(user, 100, 1, 2);
    }

    @Test
    void markSeasonWatched_shouldReturnOk() {
        User user = buildUser(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);
        List<Integer> episodes = List.of(1, 2, 3);

        ResponseEntity<Void> response = episodeWatchController.markSeasonWatched(userDetails, 100, 1, episodes);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(episodeWatchService).markSeasonWatched(user, 100, 1, episodes);
    }

    @Test
    void unmarkSeasonWatched_shouldReturnOk() {
        User user = buildUser(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        ResponseEntity<Void> response = episodeWatchController.unmarkSeasonWatched(userDetails, 100, 1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(episodeWatchService).unmarkSeasonWatched(user, 100, 1);
    }

    private User buildUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("u" + id);
        return user;
    }
}
