package com.moviemate.controller;

import com.moviemate.dto.ContentResponse;
import com.moviemate.service.ContentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ContentControllerTest {

    private ContentService contentService;
    private ContentController contentController;

    @BeforeEach
    void setUp() {
        contentService = mock(ContentService.class);
        contentController = new ContentController(contentService);
    }

    @Test
    void getAllContent_shouldReturnOk() {
        ContentResponse item = ContentResponse.builder().id(1L).title("Movie").build();
        when(contentService.getAllContent()).thenReturn(List.of(item));

        ResponseEntity<List<ContentResponse>> response = contentController.getAllContent();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(contentService).getAllContent();
    }

    @Test
    void getContentById_shouldReturnOk() {
        ContentResponse item = ContentResponse.builder().id(10L).title("Movie 10").build();
        when(contentService.getContentById(10L)).thenReturn(item);

        ResponseEntity<ContentResponse> response = contentController.getContentById(10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(item);
        verify(contentService).getContentById(10L);
    }
}
