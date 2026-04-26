package com.moviemate.controller;

import com.moviemate.dto.CommentRequest;
import com.moviemate.dto.CommentResponse;
import com.moviemate.dto.UserResponse;
import com.moviemate.entity.User;
import com.moviemate.security.CustomUserDetails;
import com.moviemate.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CommentControllerTest {

    private CommentService commentService;
    private CommentController commentController;

    @BeforeEach
    void setUp() {
        commentService = mock(CommentService.class);
        commentController = new CommentController(commentService);
    }

    @Test
    void getComments_shouldReturnOkAndList() {
        CommentResponse response = CommentResponse.builder()
                .id(1L)
                .content("Buen comentario")
                .author(UserResponse.builder().id(10L).username("chris").build())
                .ratingId(5L)
                .build();

        when(commentService.getByRating(5L)).thenReturn(List.of(response));

        ResponseEntity<List<CommentResponse>> entity = commentController.getComments(5L);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(entity.getBody()).hasSize(1);
        verify(commentService).getByRating(5L);
    }

    @Test
    void createComment_shouldReturnCreated() {
        User user = new User();
        user.setId(3L);
        user.setUsername("chris");
        CustomUserDetails userDetails = new CustomUserDetails(user);

        CommentRequest request = new CommentRequest();
        request.setContent("Texto");

        CommentResponse response = CommentResponse.builder()
                .id(2L)
                .content("Texto")
                .author(UserResponse.builder().id(3L).username("chris").build())
                .ratingId(5L)
                .build();

        when(commentService.create(user, 5L, request)).thenReturn(response);

        ResponseEntity<CommentResponse> entity = commentController.createComment(5L, request, userDetails);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(entity.getBody()).isEqualTo(response);
        verify(commentService).create(user, 5L, request);
    }

    @Test
    void deleteComment_shouldReturnNoContent() {
        User user = new User();
        user.setId(3L);
        user.setUsername("chris");
        CustomUserDetails userDetails = new CustomUserDetails(user);

        ResponseEntity<Void> entity = commentController.deleteComment(5L, 11L, userDetails);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(commentService).delete(user, 11L);
    }
}
