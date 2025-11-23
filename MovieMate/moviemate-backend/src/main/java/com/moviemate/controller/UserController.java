package com.moviemate.controller;

import com.moviemate.dto.UserProfileResponse;
import com.moviemate.dto.UserResponse;
import com.moviemate.entity.User;
import com.moviemate.security.CustomUserDetails;
import com.moviemate.service.FollowerService;
import com.moviemate.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    private final FollowerService followerService;
    
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        UserResponse userResponse = userService.mapToUserResponse(user);
        return ResponseEntity.ok(userResponse);
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        User user = userService.findUserById(userId);
        UserResponse userResponse = userService.mapToUserResponse(user);
        return ResponseEntity.ok(userResponse);
    }
    
    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        User user = userService.findUserByUsername(username);
        UserResponse userResponse = userService.mapToUserResponse(user);
        return ResponseEntity.ok(userResponse);
    }
    
    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId) {
        User targetUser = userService.findUserById(userId);
        User currentUser = userDetails.getUser();
        
        UserProfileResponse profile = new UserProfileResponse();
        profile.setId(targetUser.getId());
        profile.setUsername(targetUser.getUsername());
        profile.setEmail(targetUser.getEmail());
        profile.setAvatarUrl(targetUser.getAvatarUrl());
        profile.setBio(targetUser.getBio());
        profile.setCreatedAt(targetUser.getCreatedAt());
        
        // Estadísticas de seguidores
        profile.setFollowersCount(followerService.getFollowersCount(targetUser));
        profile.setFollowingCount(followerService.getFollowingCount(targetUser));
        profile.setIsFollowing(followerService.isFollowing(currentUser, targetUser));
        
        return ResponseEntity.ok(profile);
    }
    
    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UpdateProfileRequest request) {
        User user = userDetails.getUser();
        UserResponse updatedUser = userService.updateUserProfile(user, request.getBio(), request.getAvatarUrl());
        return ResponseEntity.ok(updatedUser);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String q) {
        List<UserResponse> users = userService.searchUsers(q);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/suggested")
    public ResponseEntity<List<UserResponse>> getSuggestedUsers(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User currentUser = userDetails.getUser();
        List<UserResponse> suggestedUsers = userService.getSuggestedUsers(currentUser);
        return ResponseEntity.ok(suggestedUsers);
    }
    
    // DTO para actualizar perfil
    public static class UpdateProfileRequest {
        private String bio;
        private String avatarUrl;
        
        // Getters y setters
        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    }
}