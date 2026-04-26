package com.moviemate.controller;

import com.moviemate.dto.AddToListRequest;
import com.moviemate.dto.ListRequest;
import com.moviemate.dto.ListResponse;
import com.moviemate.entity.User;
import com.moviemate.security.CustomUserDetails;
import com.moviemate.service.ListService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ListControllerTest {

    private ListService listService;
    private ListController listController;

    @BeforeEach
    void setUp() {
        listService = mock(ListService.class);
        listController = new ListController(listService);
    }

    @Test
    void createList_shouldReturnOk() {
        User user = buildUser(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);
        ListRequest request = new ListRequest();
        request.setName("Favoritas");
        ListResponse responseBody = ListResponse.builder().id(10L).name("Favoritas").build();
        when(listService.createList(user, request)).thenReturn(responseBody);

        ResponseEntity<ListResponse> response = listController.createList(userDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(responseBody);
        verify(listService).createList(user, request);
    }

    @Test
    void addContentToList_shouldReturnOk() {
        User user = buildUser(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);
        AddToListRequest request = new AddToListRequest();
        request.setTmdbId(1000);
        ListResponse responseBody = ListResponse.builder().id(10L).name("Lista").build();
        when(listService.addContentToList(user, 10L, 1000)).thenReturn(responseBody);

        ResponseEntity<ListResponse> response = listController.addContentToList(userDetails, 10L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(responseBody);
        verify(listService).addContentToList(user, 10L, 1000);
    }

    @Test
    void removeContentFromList_shouldReturnNoContent() {
        User user = buildUser(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        ResponseEntity<Void> response = listController.removeContentFromList(userDetails, 10L, 1000);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(listService).removeContentFromList(user, 10L, 1000);
    }

    @Test
    void deleteList_shouldReturnNoContent() {
        User user = buildUser(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        ResponseEntity<Void> response = listController.deleteList(userDetails, 10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(listService).deleteList(user, 10L);
    }

    @Test
    void updateList_shouldReturnOk() {
        User user = buildUser(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);
        ListRequest request = new ListRequest();
        request.setName("Actualizada");
        ListResponse responseBody = ListResponse.builder().id(10L).name("Actualizada").build();
        when(listService.updateList(user, 10L, request)).thenReturn(responseBody);

        ResponseEntity<ListResponse> response = listController.updateList(userDetails, 10L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(responseBody);
        verify(listService).updateList(user, 10L, request);
    }

    @Test
    void getListById_shouldHandleAuthenticatedUser() {
        User user = buildUser(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);
        ListResponse responseBody = ListResponse.builder().id(10L).name("Lista").build();
        when(listService.getListById(10L, 1L)).thenReturn(responseBody);

        ResponseEntity<ListResponse> response = listController.getListById(userDetails, 10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(responseBody);
        verify(listService).getListById(10L, 1L);
    }

    @Test
    void getPublicLists_shouldHandleAuthenticationPrincipal() {
        User user = buildUser(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        List<ListResponse> responseBody = List.of(ListResponse.builder().id(10L).name("Publica").build());
        when(listService.getPublicLists(1L)).thenReturn(responseBody);

        ResponseEntity<List<ListResponse>> response = listController.getPublicLists(authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(listService).getPublicLists(1L);
    }

    private User buildUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("u" + id);
        return user;
    }
}
