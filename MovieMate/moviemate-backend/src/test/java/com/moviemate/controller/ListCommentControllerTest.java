package com.moviemate.controller;

import com.moviemate.dto.CommentRequest;
import com.moviemate.dto.ListCommentResponse;
import com.moviemate.dto.UserResponse;
import com.moviemate.entity.User;
import com.moviemate.security.CustomUserDetails;
import com.moviemate.service.ListCommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ListCommentControllerTest {

    private ListCommentService listCommentService;
    private ListCommentController listCommentController;

    @BeforeEach
    void setUp() {
        listCommentService = mock(ListCommentService.class);
        listCommentController = new ListCommentController(listCommentService);
    }

    @Test
    void getComments_shouldReturnOk() {
        ListCommentResponse item = ListCommentResponse.builder()
                .id(1L)
                .content("Hola")
                .author(UserResponse.builder().id(2L).username("ana").build())
                .listId(10L)
                .build();
        when(listCommentService.getByList(10L)).thenReturn(List.of(item));

        ResponseEntity<List<ListCommentResponse>> response = listCommentController.getComments(10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(listCommentService).getByList(10L);
    }

    @Test
    void createComment_shouldReturnCreated() {
        User user = buildUser(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);
        CommentRequest request = new CommentRequest();
        request.setContent("Nuevo");
        ListCommentResponse created = ListCommentResponse.builder().id(8L).content("Nuevo").listId(10L).build();

        when(listCommentService.create(user, 10L, request)).thenReturn(created);

        ResponseEntity<ListCommentResponse> response = listCommentController.createComment(10L, request, userDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(created);
        verify(listCommentService).create(user, 10L, request);
    }

    @Test
    void deleteComment_shouldReturnNoContent() {
        User user = buildUser(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        ResponseEntity<Void> response = listCommentController.deleteComment(10L, 5L, userDetails);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(listCommentService).delete(user, 5L);
    }

    private User buildUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("u" + id);
        return user;
    }
}
