package com.moviemate.service;

import com.moviemate.dto.ListRequest;
import com.moviemate.dto.ListResponse;
import com.moviemate.entity.Content;
import com.moviemate.entity.List;
import com.moviemate.entity.ListContent;
import com.moviemate.entity.User;
import com.moviemate.exception.DuplicateListNameException;
import com.moviemate.repository.ContentRepository;
import com.moviemate.repository.ListContentRepository;
import com.moviemate.repository.ListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ListServiceTest {

    private ListRepository listRepository;
    private ListContentRepository listContentRepository;
    private ContentRepository contentRepository;
    private ListService listService;
    private ContentService contentService;

    @BeforeEach
    void setUp() {
        listRepository = mock(ListRepository.class);
        listContentRepository = mock(ListContentRepository.class);
        contentService = mock(ContentService.class);
        contentRepository = mock(ContentRepository.class);

        listService = new ListService(listRepository, listContentRepository, contentService);
    }

    // ---------- createList ----------

    @Test
    void createList_shouldCreateCustomList_whenNameIsUnique() {
        User user = new User();
        user.setId(1L);

        ListRequest request = new ListRequest();
        request.setName("Favoritas");
        request.setDescription("Mis pelis favoritas");
        request.setIsPublic(true);
        request.setListType(List.ListType.CUSTOM);

        when(listRepository.existsByUserAndName(user, "Favoritas")).thenReturn(false);
        when(listRepository.findByUserAndListType(user, List.ListType.CUSTOM))
                .thenReturn(Optional.empty());

        ArgumentCaptor<List> listCaptor = ArgumentCaptor.forClass(List.class);
        when(listRepository.save(listCaptor.capture())).thenAnswer(invocation -> {
            List l = listCaptor.getValue();
            l.setId(10L);
            l.setUser(user);
            l.setContents(Collections.emptyList());
            return l;
        });

        ListResponse response = listService.createList(user, request);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getName()).isEqualTo("Favoritas");
        assertThat(response.getIsPublic()).isTrue();
        assertThat(response.getListType()).isEqualTo(List.ListType.CUSTOM);

        verify(listRepository).existsByUserAndName(user, "Favoritas");
        verify(listRepository).save(any(List.class));
    }

    @Test
    void createList_shouldThrowDuplicate_whenNameExists() {
        User user = new User();
        user.setId(1L);

        ListRequest request = new ListRequest();
        request.setName("Favoritas");
        request.setListType(List.ListType.CUSTOM);

        when(listRepository.existsByUserAndName(user, "Favoritas"))
                .thenReturn(true);

        assertThatThrownBy(() -> listService.createList(user, request))
                .isInstanceOf(DuplicateListNameException.class);

        verify(listRepository, never()).save(any());
    }

    @Test
    void createList_shouldThrow_whenNonCustomTypeAlreadyExists() {
        User user = new User();
        user.setId(1L);

        ListRequest request = new ListRequest();
        request.setName("Vistas");
        request.setListType(List.ListType.WATCHED);

        when(listRepository.existsByUserAndName(user, "Vistas"))
                .thenReturn(false);

        List existing = new List();
        existing.setId(5L);
        existing.setUser(user);
        existing.setListType(List.ListType.WATCHED);

        when(listRepository.findByUserAndListType(user, List.ListType.WATCHED))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> listService.createList(user, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ya existe una lista de tipo");

        verify(listRepository, never()).save(any());
    }

    // ---------- addContentToList ----------

    @Test
    void addContentToList_shouldAdd_whenEverythingIsOk() {
        User user = new User();
        user.setId(1L);

        List list = new List();
        list.setId(10L);
        list.setUser(user);

        Content content = new Content();
        content.setId(100L);
        content.setReleaseDate(LocalDate.of(2020, 1, 1));

        when(listRepository.findById(10L)).thenReturn(Optional.of(list));
        when(contentRepository.findById(100L)).thenReturn(Optional.of(content));
        when(contentRepository.save(any(Content.class)))
            .thenAnswer(i -> i.getArgument(0));
        when(listContentRepository.existsByListAndContent(list, content)).thenReturn(false);

        // lista refrescada con un contenido
        ListContent lc = new ListContent();
        lc.setId(1L);
        lc.setList(list);
        lc.setContent(content);
        list.setContents(java.util.List.of(lc));

        when(listRepository.findById(10L)).thenReturn(Optional.of(list));

        ListResponse response = listService.addContentToList(user, 10L, 1000);

        verify(listContentRepository).save(any(ListContent.class));
        assertThat(response.getItemCount()).isEqualTo(1);
    }

    @Test
    void addContentToList_shouldThrow_whenListNotFound() {
        User user = new User();
        user.setId(1L);

        when(listRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listService.addContentToList(user, 10L, 1000))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Lista no encontrada");
    }

    @Test
    void addContentToList_shouldThrow_whenUserIsNotOwner() {
        User owner = new User();
        owner.setId(1L);

        User other = new User();
        other.setId(2L);

        List list = new List();
        list.setId(10L);
        list.setUser(owner);

        when(listRepository.findById(10L)).thenReturn(Optional.of(list));

        assertThatThrownBy(() -> listService.addContentToList(other, 10L, 1000))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No tienes permisos");
    }

    @Test
    void addContentToList_shouldThrow_whenContentNotFound() {
        User user = new User();
        user.setId(1L);

        List list = new List();
        list.setId(10L);
        list.setUser(user);

        when(listRepository.findById(10L)).thenReturn(Optional.of(list));
        when(contentService.getOrFetch(1000))
            .thenThrow(new RuntimeException("Contenido no encontrado: 1000"));

        assertThatThrownBy(() -> listService.addContentToList(user, 10L, 1000))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Contenido no encontrado");
    }

    @Test
    void addContentToList_shouldThrow_whenContentAlreadyInList() {
        User user = new User();
        user.setId(1L);

        List list = new List();
        list.setId(10L);
        list.setUser(user);

        Content content = new Content();
        content.setId(100L);
        content.setTmdbId(1000);

        when(listRepository.findById(10L)).thenReturn(Optional.of(list));
        when(contentService.getOrFetch(1000)).thenReturn(content);
        when(listContentRepository.existsByListAndContent(list, content)).thenReturn(true);

        assertThatThrownBy(() -> listService.addContentToList(user, 10L, 1000))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El contenido ya está en la lista");
    }

    // ---------- removeContentFromList ----------

    @Test
    void removeContentFromList_shouldDelete_whenEverythingIsOk() {
        User user = new User();
        user.setId(1L);

        List list = new List();
        list.setId(10L);
        list.setUser(user);

        Content content = new Content();
        content.setId(100L);
        content.setTmdbId(1000);

        ListContent listContent = new ListContent();
        listContent.setId(1L);
        listContent.setList(list);
        listContent.setContent(content);

        when(listRepository.findById(10L)).thenReturn(Optional.of(list));
        when(contentService.getOrFetch(1000)).thenReturn(content);
        when(listContentRepository.findByListAndContent(list, content))
                .thenReturn(Optional.of(listContent));

        listService.removeContentFromList(user, 10L, 1000);

        verify(listContentRepository).delete(listContent);
    }

    @Test
    void removeContentFromList_shouldThrow_whenListNotFound() {
        User user = new User();
        user.setId(1L);

        when(listRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listService.removeContentFromList(user, 10L, 1000))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Lista no encontrada");
    }

    @Test
    void removeContentFromList_shouldThrow_whenUserIsNotOwner() {
        User owner = new User();
        owner.setId(1L);

        User other = new User();
        other.setId(2L);

        List list = new List();
        list.setId(10L);
        list.setUser(owner);

        when(listRepository.findById(10L)).thenReturn(Optional.of(list));

        assertThatThrownBy(() -> listService.removeContentFromList(other, 10L, 1000))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No tienes permisos");
    }

    @Test
    void removeContentFromList_shouldThrow_whenContentNotFound() {
        User user = new User();
        user.setId(1L);

        List list = new List();
        list.setId(10L);
        list.setUser(user);

        when(listRepository.findById(10L)).thenReturn(Optional.of(list));
        when(contentService.getOrFetch(1000)).thenReturn(null);

        assertThatThrownBy(() -> listService.removeContentFromList(user, 10L, 1000))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El contenido no está en la lista");
    }

    @Test
    void removeContentFromList_shouldThrow_whenContentNotInList() {
        User user = new User();
        user.setId(1L);

        List list = new List();
        list.setId(10L);
        list.setUser(user);

        Content content = new Content();
        content.setId(100L);
        content.setTmdbId(1000);

        when(listRepository.findById(10L)).thenReturn(Optional.of(list));
        when(contentService.getOrFetch(1000)).thenReturn(content);
        when(listContentRepository.findByListAndContent(list, content))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> listService.removeContentFromList(user, 10L, 1000))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El contenido no está en la lista");
    }

    // ---------- getUserLists / getPublicLists ----------

    @Test
    void getUserLists_shouldMapResults() {
        User user = new User();
        user.setId(1L);
        user.setUsername("chris");

        List list = buildListWithOneContent(user);

        when(listRepository.findByUserWithContents(user))
                .thenReturn(java.util.List.of(list));

        java.util.List<ListResponse> responses = listService.getUserLists(user);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getItemCount()).isEqualTo(1);
        assertThat(responses.get(0).getUser().getUsername()).isEqualTo("chris");
    }

    @Test
    void getPublicLists_shouldMapResults() {
        User user = new User();
        user.setId(1L);
        user.setUsername("chris");

        List list = buildListWithOneContent(user);
        list.setIsPublic(true);

        when(listRepository.findPublicListsWithContentsForUser(user.getId()))
                .thenReturn(java.util.List.of(list));

        java.util.List<ListResponse> responses = listService.getPublicLists(user.getId());

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getItemCount()).isEqualTo(1);
        assertThat(responses.get(0).getUser().getUsername()).isEqualTo("chris");
    }

    // ---------- mapToListResponse ----------

    @Test
    void mapToListResponse_shouldHandleNullContents() {
        User user = new User();
        user.setId(1L);
        user.setUsername("chris");

        List list = new List();
        list.setId(10L);
        list.setName("Vacía");
        list.setUser(user);
        list.setContents(null);

        ListResponse response = listService.mapToListResponse(list);

        assertThat(response.getItemCount()).isEqualTo(0);
        assertThat(response.getContents()).isEmpty();
    }

    // helper para montar una lista con un contenido
    private List buildListWithOneContent(User user) {
        Content content = new Content();
        content.setId(100L);
        content.setTmdbId(1000);
        content.setTitle("Peli");
        content.setContentType( Content.ContentType.MOVIE);
        content.setReleaseDate(LocalDate.of(2020, 1, 1));
        content.setPosterUrl("poster.jpg");
        content.setBackdropUrl("backdrop.jpg");
        content.setSynopsis("Sinopsis");
        content.setGenres(new java.util.ArrayList<>(java.util.List.of("Acción", "Aventura")));
        content.setTmdbRating(8.5);
        content.setTmdbVoteCount(1000);
        content.setAppRating(9.0);
        content.setAppVoteCount(100);
        content.setLastTmdbSync(LocalDateTime.now().minusDays(1));
        content.setLastInteraction(LocalDateTime.now().minusHours(5));
        content.setSyncStatus(Content.SyncStatus.FRESH);

        List list = new List();
        list.setId(10L);
        list.setName("Lista");
        list.setDescription("Desc");
        list.setIsPublic(true);
        list.setListType(List.ListType.CUSTOM);
        list.setCreatedAt(LocalDateTime.now());
        list.setUser(user);

        ListContent lc = new ListContent();
        lc.setId(1L);
        lc.setList(list);
        lc.setContent(content);

        list.setContents(java.util.List.of(lc));

        return list;
    }
}
