package com.moviemate.controller;

import com.moviemate.dto.CommentRequest;
import com.moviemate.dto.CommentResponse;
import com.moviemate.entity.User;
import com.moviemate.security.CustomUserDetails;
import com.moviemate.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ratings/{ratingId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable Long ratingId) {
        return ResponseEntity.ok(commentService.getByRating(ratingId));
    }

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long ratingId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        CommentResponse response = commentService.create(user, ratingId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long ratingId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        commentService.delete(user, commentId);
        return ResponseEntity.noContent().build();
    }
}
