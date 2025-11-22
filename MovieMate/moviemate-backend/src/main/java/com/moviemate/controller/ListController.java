package com.moviemate.controller;

import com.moviemate.dto.ListRequest;
import com.moviemate.dto.ListResponse;
import com.moviemate.entity.User;
import com.moviemate.security.CustomUserDetails;
import com.moviemate.service.ListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lists")
@RequiredArgsConstructor
public class ListController {
    
    private final ListService listService;
    
    @PostMapping
    public ResponseEntity<ListResponse> createList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ListRequest request) {
        User user = userDetails.getUser();
        return ResponseEntity.ok(listService.createList(user, request));
    }
    
    @PostMapping("/{listId}/content/{contentId}")
    public ResponseEntity<ListResponse> addContentToList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long listId,
            @PathVariable Long contentId) {
        User user = userDetails.getUser();
        return ResponseEntity.ok(listService.addContentToList(user, listId, contentId));
    }
    
    @DeleteMapping("/{listId}/content/{contentId}")
    public ResponseEntity<Void> removeContentFromList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long listId,
            @PathVariable Long contentId) {
        User user = userDetails.getUser();
        listService.removeContentFromList(user, listId, contentId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/my-lists")
    public ResponseEntity<List<ListResponse>> getUserLists(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        return ResponseEntity.ok(listService.getUserLists(user));
    }
    
    @GetMapping("/public")
    public ResponseEntity<List<ListResponse>> getPublicLists() {
        return ResponseEntity.ok(listService.getPublicLists());
    }
}