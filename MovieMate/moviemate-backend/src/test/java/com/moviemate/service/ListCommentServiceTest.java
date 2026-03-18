package com.moviemate.service;

import com.moviemate.dto.CommentRequest;
import com.moviemate.dto.ListCommentResponse;
import com.moviemate.entity.ListComment;
import com.moviemate.entity.User;
import com.moviemate.repository.ListCommentRepository;
import com.moviemate.repository.ListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ListCommentServiceTest {

    private ListCommentRepository listCommentRepository;
    private ListRepository listRepository;
    private ListCommentService listCommentService;

    @BeforeEach
    void setUp() {
        listCommentRepository = mock(ListCommentRepository.class);
        listRepository = mock(ListRepository.class);
        listCommentService = new ListCommentService(listCommentRepository, listRepository);
    }

    // ---------- getByList ----------

    @Test
    void getByList_shouldReturnMappedComments() {
        User author = buildUser(1L, "alice");
        com.moviemate.entity.List list = buildList(10L, author);
        ListComment c1 = buildComment(1L, "Great list!", author, list);
        ListComment c2 = buildComment(2L, "Love it", author, list);

        when(listCommentRepository.findByListIdAndDeletedFalseOrderByCreatedAtAsc(10L))
                .thenReturn(List.of(c1, c2));

        List<ListCommentResponse> result = listCommentService.getByList(10L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getContent()).isEqualTo("Great list!");
        assertThat(result.get(1).getContent()).isEqualTo("Love it");
        verify(listCommentRepository).findByListIdAndDeletedFalseOrderByCreatedAtAsc(10L);
    }

    @Test
    void getByList_shouldReturnEmpty_whenNoComments() {
        when(listCommentRepository.findByListIdAndDeletedFalseOrderByCreatedAtAsc(10L))
                .thenReturn(List.of());

        List<ListCommentResponse> result = listCommentService.getByList(10L);

        assertThat(result).isEmpty();
    }

    // ---------- create ----------

    @Test
    void create_shouldSaveAndReturnComment() {
        User author = buildUser(1L, "alice");
        com.moviemate.entity.List list = buildList(10L, author);
        CommentRequest request = new CommentRequest();
        request.setContent("Nice one!");

        ListComment savedComment = buildComment(1L, "Nice one!", author, list);

        when(listRepository.findById(10L)).thenReturn(Optional.of(list));
        when(listCommentRepository.save(any(ListComment.class))).thenReturn(savedComment);

        ListCommentResponse result = listCommentService.create(author, 10L, request);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("Nice one!");
        assertThat(result.getListId()).isEqualTo(10L);
        assertThat(result.getAuthor().getUsername()).isEqualTo("alice");
        verify(listRepository).findById(10L);
        verify(listCommentRepository).save(any(ListComment.class));
    }

    @Test
    void create_shouldThrow_whenListNotFound() {
        User author = buildUser(1L, "alice");
        CommentRequest request = new CommentRequest();
        request.setContent("Test");

        when(listRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listCommentService.create(author, 999L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Lista no encontrada");

        verify(listCommentRepository, never()).save(any());
    }

    // ---------- delete ----------

    @Test
    void delete_shouldSoftDelete_whenUserIsAuthor() {
        User author = buildUser(1L, "alice");
        com.moviemate.entity.List list = buildList(10L, author);
        ListComment comment = buildComment(1L, "Hello", author, list);

        when(listCommentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(listCommentRepository.save(any(ListComment.class))).thenReturn(comment);

        listCommentService.delete(author, 1L);

        assertThat(comment.isDeleted()).isTrue();
        verify(listCommentRepository).save(comment);
    }

    @Test
    void delete_shouldThrow_whenCommentNotFound() {
        User user = buildUser(1L, "alice");
        when(listCommentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listCommentService.delete(user, 999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Comentario no encontrado");

        verify(listCommentRepository, never()).save(any());
    }

    @Test
    void delete_shouldThrow_whenUserIsNotAuthor() {
        User author = buildUser(1L, "alice");
        User other = buildUser(2L, "bob");
        com.moviemate.entity.List list = buildList(10L, author);
        ListComment comment = buildComment(1L, "Hello", author, list);

        when(listCommentRepository.findById(1L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> listCommentService.delete(other, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("permisos");

        verify(listCommentRepository, never()).save(any());
    }

    // ---------- mapToResponse ----------

    @Test
    void mapToResponse_shouldMapAllFields() {
        User author = buildUser(1L, "alice");
        author.setAvatarUrl("avatar.jpg");
        com.moviemate.entity.List list = buildList(10L, author);
        ListComment comment = buildComment(5L, "Test comment", author, list);

        ListCommentResponse result = listCommentService.mapToResponse(comment);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getContent()).isEqualTo("Test comment");
        assertThat(result.getListId()).isEqualTo(10L);
        assertThat(result.getAuthor().getId()).isEqualTo(1L);
        assertThat(result.getAuthor().getUsername()).isEqualTo("alice");
        assertThat(result.getAuthor().getAvatarUrl()).isEqualTo("avatar.jpg");
    }

    // ---------- helpers ----------

    private User buildUser(Long id, String username) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        return u;
    }

    private com.moviemate.entity.List buildList(Long id, User owner) {
        com.moviemate.entity.List l = new com.moviemate.entity.List();
        l.setId(id);
        l.setUser(owner);
        l.setName("My List");
        return l;
    }

    private ListComment buildComment(Long id, String content, User author, com.moviemate.entity.List list) {
        ListComment c = new ListComment();
        c.setId(id);
        c.setContent(content);
        c.setAuthor(author);
        c.setList(list);
        c.setDeleted(false);
        c.setCreatedAt(LocalDateTime.now());
        c.setUpdatedAt(LocalDateTime.now());
        return c;
    }
}
