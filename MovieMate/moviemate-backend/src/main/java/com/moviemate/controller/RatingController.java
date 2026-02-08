package com.moviemate.controller;

import com.moviemate.dto.RatingRequest;
import com.moviemate.dto.RatingResponse;
import com.moviemate.entity.User;
import com.moviemate.security.CustomUserDetails;
import com.moviemate.service.RatingService;
import com.moviemate.service.ReviewLikeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {
    
    private final RatingService ratingService;
    private final ReviewLikeService reviewLikeService;

    @Operation(
            summary = "Crear o actualizar una puntuación",
            requestBody = @RequestBody(
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "Ejemplo rating",
                                    value = """
                                    {
                                      "tmdbId": 1084242,
                                      "contentType": "MOVIE",
                                      "rating": 4,
                                      "reviewText": "Gran película.",
                                      "emotionalTag": "INCREIBLE",
                                      "status": "VISTA",
                                      "watchedDate": "2025-12-02"
                                    }
                                    """
                            )
                    )
            )
    )
    @PostMapping
    public ResponseEntity<RatingResponse> createOrUpdateRating(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @org.springframework.web.bind.annotation.RequestBody RatingRequest request) {
        User user = userDetails.getUser();
        return ResponseEntity.ok(ratingService.createOrUpdateRating(user, request));
    }

    @Operation(summary = "Obtener las puntuaciones de un contenido específico")
    @GetMapping("/{contentId}")
    public ResponseEntity<List<RatingResponse>> getRatingsByContent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long contentId) {
        User user = userDetails.getUser();
        List<RatingResponse> response = ratingService.getRatingsByContent(user, contentId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Eliminar una puntuación existente")
    @DeleteMapping("/{ratingId}")
    public ResponseEntity<Void> deleteRating(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long ratingId) {
        User user = userDetails.getUser();
        ratingService.deleteRating(user, ratingId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Dar o quitar like a una puntuación")
    @PostMapping("/{ratingId}/likes")
    public ResponseEntity<Void> toggleLike(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long ratingId) {

        User user = userDetails.getUser();
        reviewLikeService.toggleLike(user, ratingId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Obtener la cantidad de likes de una puntuación")
    @GetMapping("/{ratingId}/likes")
    public ResponseEntity<Integer> getLikesCount(@PathVariable Long ratingId) {
        return ResponseEntity.ok(reviewLikeService.getLikesCount(ratingId));
    }

    @Operation(summary = "Comprobar si el usuario ha dado like a una puntuación")
    @GetMapping("/{ratingId}/like-status")
    public ResponseEntity<Boolean> hasLiked(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long ratingId) {

        User user = userDetails.getUser();
        return ResponseEntity.ok(reviewLikeService.hasLiked(user, ratingId));
    }
}
