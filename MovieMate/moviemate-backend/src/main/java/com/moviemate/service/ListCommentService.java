package com.moviemate.service;

import com.moviemate.dto.CommentRequest;
import com.moviemate.dto.ListCommentResponse;
import com.moviemate.dto.UserResponse;
import com.moviemate.entity.List;
import com.moviemate.entity.ListComment;
import com.moviemate.entity.User;
import com.moviemate.repository.ListCommentRepository;
import com.moviemate.repository.ListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListCommentService {

    private final ListCommentRepository listCommentRepository;
    private final ListRepository listRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public java.util.List<ListCommentResponse> getByList(Long listId) {
        return listCommentRepository.findByListIdAndDeletedFalseOrderByCreatedAtAsc(listId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ListCommentResponse create(User author, Long listId, CommentRequest request) {
        List list = listRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("Lista no encontrada"));

        ListComment comment = new ListComment();
        comment.setContent(request.getContent());
        comment.setAuthor(author);
        comment.setList(list);

        ListComment saved = listCommentRepository.save(comment);

        // Notificar al dueño de la lista si no es el mismo que comenta
        User listOwner = list.getUser();
        if (!listOwner.getId().equals(author.getId())) {
            try {
                notificationService.notifyListComment(listOwner.getId(), author.getId(), saved.getId(), list.getName());
            } catch (Exception e) {
                log.warn("No se pudo enviar notificación de comentario en lista: {}", e.getMessage());
            }
        }

        return mapToResponse(saved);
    }

    @Transactional
    public void delete(User user, Long commentId) {
        ListComment comment = listCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));

        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("No tienes permisos para eliminar este comentario");
        }

        comment.setDeleted(true);
        listCommentRepository.save(comment);
    }

    public ListCommentResponse mapToResponse(ListComment comment) {
        return ListCommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .listId(comment.getList().getId())
                .author(UserResponse.builder()
                        .id(comment.getAuthor().getId())
                        .username(comment.getAuthor().getUsername())
                        .avatarUrl(comment.getAuthor().getAvatarUrl())
                        .build())
                .build();
    }
}
