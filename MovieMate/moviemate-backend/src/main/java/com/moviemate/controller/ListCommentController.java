package com.moviemate.controller;

import com.moviemate.dto.CommentRequest;
import com.moviemate.dto.ListCommentResponse;
import com.moviemate.entity.User;
import com.moviemate.security.CustomUserDetails;
import com.moviemate.service.ListCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lists/{listId}/comments")
@RequiredArgsConstructor
public class ListCommentController {

    private final ListCommentService listCommentService;

    @GetMapping
    public ResponseEntity<List<ListCommentResponse>> getComments(
            @PathVariable Long listId) {
        return ResponseEntity.ok(listCommentService.getByList(listId));
    }

    @PostMapping
    public ResponseEntity<ListCommentResponse> createComment(
            @PathVariable Long listId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        ListCommentResponse response = listCommentService.create(user, listId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long listId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        listCommentService.delete(user, commentId);
        return ResponseEntity.noContent().build();
    }
}
