package com.moviemate.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moviemate.security.CustomUserDetails;
import com.moviemate.service.NotificationService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long notificationId) {

        notificationService.markAsRead(notificationId, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        notificationService.markAllAsRead(userDetails.getUser());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("unread-count")
    public ResponseEntity<Integer> getUnreadCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        int count = notificationService.getUnreadCount(userDetails.getUser());
        return ResponseEntity.ok(count);
    }
    


}