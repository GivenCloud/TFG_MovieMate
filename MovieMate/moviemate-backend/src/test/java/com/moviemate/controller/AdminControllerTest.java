package com.moviemate.controller;

import com.moviemate.dto.CommentResponse;
import com.moviemate.dto.RatingResponse;
import com.moviemate.dto.ReportResponse;
import com.moviemate.dto.UserResponse;
import com.moviemate.entity.ContentReport;
import com.moviemate.entity.Role;
import com.moviemate.entity.User;
import com.moviemate.repository.UserRepository;
import com.moviemate.service.CommentService;
import com.moviemate.service.ContentReportService;
import com.moviemate.service.RatingService;
import com.moviemate.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AdminControllerTest {

    private UserRepository userRepository;
    private UserService userService;
    private RatingService ratingService;
    private CommentService commentService;
    private ContentReportService reportService;
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = mock(UserService.class);
        ratingService = mock(RatingService.class);
        commentService = mock(CommentService.class);
        reportService = mock(ContentReportService.class);
        adminController = new AdminController(userRepository, userService, ratingService, commentService, reportService);
    }

    @Test
    void listUsers_shouldUseFindAllWhenNoQuery() {
        User user = buildUser(1L, "ana");
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userService.mapToUserResponse(user)).thenReturn(UserResponse.builder().id(1L).username("ana").build());

        ResponseEntity<List<UserResponse>> response = adminController.listUsers(null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(userRepository).findAll();
    }

    @Test
    void listUsers_shouldUseSearchWhenQuery() {
        User user = buildUser(1L, "ana");
        when(userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase("an", "an"))
                .thenReturn(List.of(user));
        when(userService.mapToUserResponse(user)).thenReturn(UserResponse.builder().id(1L).username("ana").build());

        ResponseEntity<List<UserResponse>> response = adminController.listUsers("an");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(userRepository).findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase("an", "an");
    }

    @Test
    void changeRole_shouldReturnUpdatedUser() {
        User user = buildUser(1L, "ana");
        user.setRole(Role.USER);
        when(userService.findUserById(1L)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userService.mapToUserResponse(user)).thenReturn(UserResponse.builder().id(1L).username("ana").build());

        ResponseEntity<UserResponse> response = adminController.changeRole(1L, Map.of("role", "ADMIN"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void banUser_shouldReturnUpdatedUser() {
        User user = buildUser(1L, "ana");
        when(userService.findUserById(1L)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userService.mapToUserResponse(user)).thenReturn(UserResponse.builder().id(1L).username("ana").build());

        ResponseEntity<UserResponse> response = adminController.banUser(1L, Map.of("banned", true));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(user.getBanned()).isTrue();
    }

    @Test
    void getRating_shouldReturnOk() {
        RatingResponse payload = RatingResponse.builder().id(10L).build();
        when(ratingService.getRatingById(10L)).thenReturn(payload);

        ResponseEntity<RatingResponse> response = adminController.getRating(10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(payload);
    }

    @Test
    void deleteRating_shouldReturnNoContent() {
        ResponseEntity<Void> response = adminController.deleteRating(10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(ratingService).adminDeleteRating(10L);
    }

    @Test
    void getComment_shouldReturnOk() {
        CommentResponse payload = CommentResponse.builder().id(10L).build();
        when(commentService.getCommentById(10L)).thenReturn(payload);

        ResponseEntity<CommentResponse> response = adminController.getComment(10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(payload);
    }

    @Test
    void deleteComment_shouldReturnNoContent() {
        ResponseEntity<Void> response = adminController.deleteComment(10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(commentService).adminDelete(10L);
    }

    @Test
    void getReports_shouldReturnOk() {
        ReportResponse payload = ReportResponse.builder()
                .id(1L)
                .targetType(ContentReport.TargetType.RATING)
                .targetId(10L)
                .reason(ContentReport.ReportReason.SPAM)
                .status(ContentReport.ReportStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        when(reportService.getReports(ContentReport.ReportStatus.PENDING)).thenReturn(List.of(payload));

        ResponseEntity<List<ReportResponse>> response = adminController.getReports(ContentReport.ReportStatus.PENDING);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void resolveAndDismissReport_shouldReturnOk() {
        ReportResponse resolved = ReportResponse.builder().id(1L).status(ContentReport.ReportStatus.RESOLVED).build();
        ReportResponse dismissed = ReportResponse.builder().id(1L).status(ContentReport.ReportStatus.DISMISSED).build();
        when(reportService.resolveReport(1L)).thenReturn(resolved);
        when(reportService.dismissReport(1L)).thenReturn(dismissed);

        ResponseEntity<ReportResponse> resolveResponse = adminController.resolveReport(1L);
        ResponseEntity<ReportResponse> dismissResponse = adminController.dismissReport(1L);

        assertThat(resolveResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resolveResponse.getBody()).isEqualTo(resolved);
        assertThat(dismissResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(dismissResponse.getBody()).isEqualTo(dismissed);
    }

    private User buildUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@mail.com");
        return user;
    }
}
