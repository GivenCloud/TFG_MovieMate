package com.moviemate.service;

import com.moviemate.dto.CommentRequest;
import com.moviemate.dto.CommentResponse;
import com.moviemate.entity.Comment;
import com.moviemate.entity.Content;
import com.moviemate.entity.Rating;
import com.moviemate.entity.User;
import com.moviemate.repository.CommentRepository;
import com.moviemate.repository.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CommentServiceTest {

    private CommentRepository commentRepository;
    private RatingRepository ratingRepository;
    private NotificationService notificationService;
    private CommentService commentService;

    @BeforeEach
    void setUp() {
        commentRepository = mock(CommentRepository.class);
        ratingRepository = mock(RatingRepository.class);
        notificationService = mock(NotificationService.class);
        commentService = new CommentService(commentRepository, ratingRepository, notificationService);
    }

    @Test
    void getByRating_shouldMapComments() {
        Comment comment = buildComment(1L, "Buen comentario");

        when(commentRepository.findByRatingIdAndDeletedFalseOrderByCreatedAtAsc(10L))
                .thenReturn(List.of(comment));

        List<CommentResponse> responses = commentService.getByRating(10L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        assertThat(responses.get(0).getContent()).isEqualTo("Buen comentario");
        assertThat(responses.get(0).getAuthor().getUsername()).isEqualTo("author");
        verify(commentRepository).findByRatingIdAndDeletedFalseOrderByCreatedAtAsc(10L);
    }

    @Test
    void create_shouldSaveAndNotify_whenAuthorDiffersFromRatingOwner() {
        User author = buildUser(1L, "commenter");
        User ratingAuthor = buildUser(2L, "owner");
        Rating rating = buildRating(10L, ratingAuthor);

        CommentRequest request = new CommentRequest();
        request.setContent("Me gusta");

        when(ratingRepository.findById(10L)).thenReturn(Optional.of(rating));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment saved = invocation.getArgument(0);
            saved.setId(99L);
            saved.setCreatedAt(LocalDateTime.now());
            saved.setUpdatedAt(LocalDateTime.now());
            return saved;
        });

        CommentResponse response = commentService.create(author, 10L, request);

        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getContent()).isEqualTo("Me gusta");
        assertThat(response.getRatingId()).isEqualTo(10L);
        verify(notificationService).notifyComment(2L, 1L, 99L, "Peli");
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void create_shouldNotNotify_whenAuthorIsRatingOwner() {
        User author = buildUser(1L, "owner");
        Rating rating = buildRating(10L, author);

        CommentRequest request = new CommentRequest();
        request.setContent("Yo mismo");

        when(ratingRepository.findById(10L)).thenReturn(Optional.of(rating));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment saved = invocation.getArgument(0);
            saved.setId(101L);
            return saved;
        });

        commentService.create(author, 10L, request);

        verify(notificationService, never()).notifyComment(any(), any(), any(), any());
    }

    @Test
    void create_shouldThrow_whenRatingNotFound() {
        User author = buildUser(1L, "commenter");

        CommentRequest request = new CommentRequest();
        request.setContent("Me gusta");

        when(ratingRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.create(author, 10L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Valoración no encontrada");
    }

    @Test
    void delete_shouldSoftDelete_whenUserIsAuthor() {
        User author = buildUser(1L, "author");
        Comment comment = buildComment(7L, "Texto");
        comment.setAuthor(author);

        when(commentRepository.findById(7L)).thenReturn(Optional.of(comment));

        commentService.delete(author, 7L);

        assertThat(comment.isDeleted()).isTrue();
        verify(commentRepository).save(comment);
    }

    @Test
    void delete_shouldThrow_whenUserIsNotAuthor() {
        User author = buildUser(1L, "author");
        User otherUser = buildUser(2L, "other");
        Comment comment = buildComment(7L, "Texto");
        comment.setAuthor(author);

        when(commentRepository.findById(7L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.delete(otherUser, 7L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No tienes permisos");
        verify(commentRepository, never()).save(any());
    }

    @Test
    void getCommentById_shouldReturnResponse() {
        Comment comment = buildComment(7L, "Texto");
        when(commentRepository.findById(7L)).thenReturn(Optional.of(comment));

        CommentResponse response = commentService.getCommentById(7L);

        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getAuthor().getUsername()).isEqualTo("author");
    }

    @Test
    void getCommentAuthor_shouldReturnAuthor() {
        Comment comment = buildComment(7L, "Texto");
        when(commentRepository.findById(7L)).thenReturn(Optional.of(comment));

        User author = commentService.getCommentAuthor(7L);

        assertThat(author.getUsername()).isEqualTo("author");
    }

    @Test
    void adminDelete_shouldSoftDelete() {
        Comment comment = buildComment(7L, "Texto");
        when(commentRepository.findById(7L)).thenReturn(Optional.of(comment));

        commentService.adminDelete(7L);

        assertThat(comment.isDeleted()).isTrue();
        verify(commentRepository).save(comment);
    }

    private Comment buildComment(Long id, String content) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setContent(content);
        comment.setAuthor(buildUser(1L, "author")); 
        comment.setRating(buildRating(10L, buildUser(2L, "rating-owner")));
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        return comment;
    }

    private Rating buildRating(Long id, User owner) {
        Content content = new Content();
        content.setId(50L);
        content.setTitle("Peli");

        Rating rating = new Rating();
        rating.setId(id);
        rating.setUser(owner);
        rating.setContent(content);
        rating.setRating(5);
        return rating;
    }

    private User buildUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setAvatarUrl("avatar.png");
        return user;
    }
}