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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListCommentService {

    private final ListCommentRepository listCommentRepository;
    private final ListRepository listRepository;

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

        return mapToResponse(listCommentRepository.save(comment));
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
