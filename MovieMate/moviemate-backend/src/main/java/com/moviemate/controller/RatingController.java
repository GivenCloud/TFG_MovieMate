package com.moviemate.controller;

import com.moviemate.dto.RatingRequest;
import com.moviemate.dto.RatingResponse;
import com.moviemate.entity.User;
import com.moviemate.security.CustomUserDetails;
import com.moviemate.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {
    
    private final RatingService ratingService;
    
    @PostMapping
    public ResponseEntity<RatingResponse> createOrUpdateRating(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody RatingRequest request) {
        User user = userDetails.getUser();
        return ResponseEntity.ok(ratingService.createOrUpdateRating(user, request));
    }
    
    @GetMapping("/my-ratings")
    public ResponseEntity<List<RatingResponse>> getUserRatings(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        return ResponseEntity.ok(ratingService.getUserRatings(user));
    }
}