package com.moviemate.controller;

import com.moviemate.entity.User;
import com.moviemate.security.CustomUserDetails;
import com.moviemate.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class NotificationControllerTest {

    private NotificationService notificationService;
    private NotificationController notificationController;

    @BeforeEach
    void setUp() {
        notificationService = mock(NotificationService.class);
        notificationController = new NotificationController(notificationService);
    }

    @Test
    void markAsRead_shouldReturnNoContent() {
        User user = buildUser(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        ResponseEntity<Void> response = notificationController.markAsRead(userDetails, 9L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(notificationService).markAsRead(9L, user);
    }

    @Test
    void markAllAsRead_shouldReturnNoContent() {
        User user = buildUser(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        ResponseEntity<Void> response = notificationController.markAllAsRead(userDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(notificationService).markAllAsRead(user);
    }

    @Test
    void getUnreadCount_shouldReturnOk() {
        User user = buildUser(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);
        when(notificationService.getUnreadCount(user)).thenReturn(3);

        ResponseEntity<Integer> response = notificationController.getUnreadCount(userDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(3);
        verify(notificationService).getUnreadCount(user);
    }

    private User buildUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("u" + id);
        return user;
    }
}
