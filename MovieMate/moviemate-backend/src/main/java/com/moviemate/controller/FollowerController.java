package com.moviemate.controller;

import com.moviemate.annotation.RequirePublicProfile;
import com.moviemate.dto.FollowRequestActionResponse;
import com.moviemate.dto.UserResponse;
import com.moviemate.entity.User;
import com.moviemate.security.CustomUserDetails;
import com.moviemate.service.FollowRequestService;
import com.moviemate.service.FollowerService;
import com.moviemate.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class FollowerController {
    
    private final FollowerService followerService;
    private final UserService userService;
    private final FollowRequestService followRequestService;

    @Operation(
            summary = "Seguir a un usuario",
            description = "Permite que el usuario autenticado siga a otro usuario."
    )
    @PostMapping("/{userId}/follow-requests")
    public ResponseEntity<Void> sendFollowRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "ID del usuario al que se quiere seguir", example = "1")
            @PathVariable Long userId) {

        User currentUser = userDetails.getUser();
        User userToFollow = userService.findUserById(userId);
        followRequestService.sendFollowRequest(currentUser, userToFollow);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/follow-requests/{requestId}/accept")
    public ResponseEntity<FollowRequestActionResponse> acceptFollowRequest(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long requestId) {
        FollowRequestActionResponse response = followRequestService.acceptRequest(requestId, userDetails.getUser());
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/follow-requests/{requestId}")
    public ResponseEntity<FollowRequestActionResponse> rejectFollowRequest(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long requestId) {
        FollowRequestActionResponse response = followRequestService.rejectRequest(requestId, userDetails.getUser());
        return ResponseEntity.ok(response);
}

    @Operation(
            summary = "Dejar de seguir a un usuario",
            description = "Permite que el usuario autenticado deje de seguir a otro usuario."
    )
    @DeleteMapping("/{userId}/followers")
    public ResponseEntity<Void> unfollowUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "ID del usuario al que se quiere dejar de seguir", example = "1")
            @PathVariable Long userId) {

        User currentUser = userDetails.getUser();
        User userToUnfollow = userService.findUserById(userId);
        followerService.unfollowUser(currentUser, userToUnfollow);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Obtener seguidores de un usuario",
            description = "Devuelve una lista de usuarios que siguen al usuario con el ID dado."
    )
    @GetMapping("/{userId}/followers")
    @RequirePublicProfile(userId = "#userId")
    public ResponseEntity<List<UserResponse>> getFollowers(
            @Parameter(description = "ID del usuario cuyos seguidores se quieren consultar", example = "2")
            @PathVariable Long userId) {

        List<UserResponse> followers = followerService.getFollowers(userId);
        return ResponseEntity.ok(followers);
    }

    @Operation(
            summary = "Obtener usuarios seguidos por un usuario",
            description = "Devuelve una lista de usuarios que el usuario con el ID dado está siguiendo."
    )
    @GetMapping("/{userId}/following")
    @RequirePublicProfile(userId = "#userId")
    public ResponseEntity<List<UserResponse>> getFollowing(
            @Parameter(description = "ID del usuario cuyos seguidos se quieren consultar", example = "2")
            @PathVariable Long userId) {

        List<UserResponse> following = followerService.getFollowing(userId);
        return ResponseEntity.ok(following);
    }

    @Operation(
            summary = "Comprobar si el usuario autenticado sigue a otro usuario",
            description = "Devuelve true o false dependiendo de si el usuario autenticado sigue al usuario objetivo."
    )
    @GetMapping("/{userId}/following-status")
    @RequirePublicProfile(userId = "#userId")
    public ResponseEntity<Boolean> isFollowing(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "ID del usuario objetivo a comprobar", example = "2")
            @PathVariable Long userId) {

        User currentUser = userDetails.getUser();
        User targetUser = userService.findUserById(userId);
        boolean isFollowing = followerService.isFollowing(currentUser, targetUser);

        return ResponseEntity.ok(isFollowing);
    }
}
