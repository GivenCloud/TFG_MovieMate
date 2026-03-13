package com.moviemate.service;

import com.moviemate.dto.CommentRequest;
import com.moviemate.dto.CommentResponse;
import com.moviemate.dto.UserResponse;
import com.moviemate.entity.Comment;
import com.moviemate.entity.Rating;
import com.moviemate.entity.User;
import com.moviemate.repository.CommentRepository;
import com.moviemate.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final RatingRepository ratingRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<CommentResponse> getByRating(Long ratingId) {
        return commentRepository.findByRatingIdAndDeletedFalseOrderByCreatedAtAsc(ratingId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponse create(User author, Long ratingId, CommentRequest request) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new RuntimeException("Valoración no encontrada"));

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setAuthor(author);
        comment.setRating(rating);

        Comment saved = commentRepository.save(comment);

        // Notificar al autor de la valoración si no es el mismo que comenta
        User ratingAuthor = rating.getUser();
        if (!ratingAuthor.getId().equals(author.getId())) {
            notificationService.notifyComment(ratingAuthor, saved);
        }

        return mapToResponse(saved);
    }

    @Transactional
    public void delete(User user, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));

        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("No tienes permisos para eliminar este comentario");
        }

        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public CommentResponse getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));
        return mapToResponse(comment);
    }

    public User getCommentAuthor(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"))
                .getAuthor();
    }

    @Transactional
    public void adminDelete(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));
        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    public CommentResponse mapToResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .ratingId(comment.getRating().getId())
                .author(UserResponse.builder()
                        .id(comment.getAuthor().getId())
                        .username(comment.getAuthor().getUsername())
                        .avatarUrl(comment.getAuthor().getAvatarUrl())
                        .build())
                .build();
    }
}
