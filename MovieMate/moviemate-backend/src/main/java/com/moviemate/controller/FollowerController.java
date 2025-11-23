package com.moviemate.controller;

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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class FollowerController {
    
    private final FollowerService followerService;
    private final UserService userService;
    
    @PostMapping("/{userId}/follow")
    public ResponseEntity<Void> followUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId) {
        User currentUser = userDetails.getUser();
        User userToFollow = userService.findUserById(userId);
        followerService.followUser(currentUser, userToFollow);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{userId}/follow")
    public ResponseEntity<Void> unfollowUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId) {
        User currentUser = userDetails.getUser();
        User userToUnfollow = userService.findUserById(userId);
        followerService.unfollowUser(currentUser, userToUnfollow);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<UserResponse>> getFollowers(@PathVariable Long userId) {
        User user = userService.findUserById(userId);
        List<UserResponse> followers = followerService.getFollowers(user);
        return ResponseEntity.ok(followers);
    }
    
    @GetMapping("/{userId}/following")
    public ResponseEntity<List<UserResponse>> getFollowing(@PathVariable Long userId) {
        User user = userService.findUserById(userId);
        List<User> following = followerService.getFollowing(user);
        List<UserResponse> followingResponses = following.stream()
            .map(userService::mapToUserResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(followingResponses);
    }
    
    @GetMapping("/{userId}/is-following")
    public ResponseEntity<Boolean> isFollowing(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId) {
        User currentUser = userDetails.getUser();
        User targetUser = userService.findUserById(userId);
        boolean isFollowing = followerService.isFollowing(currentUser, targetUser);
        return ResponseEntity.ok(isFollowing);
    }
}