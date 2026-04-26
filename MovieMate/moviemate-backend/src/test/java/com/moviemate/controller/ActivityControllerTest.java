package com.moviemate.controller;

import com.moviemate.dto.ActivityResponse;
import com.moviemate.entity.User;
import com.moviemate.security.CustomUserDetails;
import com.moviemate.service.ActivityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ActivityControllerTest {

    private ActivityService activityService;
    private ActivityController activityController;

    @BeforeEach
    void setUp() {
        activityService = mock(ActivityService.class);
        activityController = new ActivityController(activityService);
    }

    @Test
    void getPersonalFeed_shouldReturnOk() {
        User user = buildUser(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Page<ActivityResponse> page = new PageImpl<>(List.of(ActivityResponse.builder().build()));

        when(activityService.getUserFeed(eq(user), any(PageRequest.class))).thenReturn(page);

        ResponseEntity<Page<ActivityResponse>> response = activityController.getPersonalFeed(userDetails, 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(page);
        verify(activityService).getUserFeed(eq(user), any(PageRequest.class));
    }

    @Test
    void getGlobalFeed_shouldReturnOk() {
        Page<ActivityResponse> page = new PageImpl<>(List.of(ActivityResponse.builder().build()));
        when(activityService.getGlobalActivity(any(PageRequest.class))).thenReturn(page);

        ResponseEntity<Page<ActivityResponse>> response = activityController.getGlobalFeed(0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(page);
        verify(activityService).getGlobalActivity(any(PageRequest.class));
    }

    private User buildUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("u" + id);
        return user;
    }
}
