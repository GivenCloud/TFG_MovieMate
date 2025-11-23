package com.moviemate.controller;

import com.moviemate.dto.ActivityResponse;
import com.moviemate.entity.User;
import com.moviemate.security.CustomUserDetails;
import com.moviemate.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class ActivityController {
    
    private final ActivityService activityService;
    
    @GetMapping
    public ResponseEntity<List<ActivityResponse>> getFeed(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        List<ActivityResponse> feed = activityService.getFeed(user);
        return ResponseEntity.ok(feed);
    }
}