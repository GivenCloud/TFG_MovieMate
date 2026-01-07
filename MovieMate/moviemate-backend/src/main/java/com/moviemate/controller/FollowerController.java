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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class FollowerController {
    
    private final FollowerService followerService;
    private final UserService userService;

    @Operation(
            summary = "Seguir a un usuario",
            description = "Permite que el usuario autenticado siga a otro usuario."
    )
    @PostMapping("/{userId}/follow")
    public ResponseEntity<Void> followUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "ID del usuario al que se quiere seguir", example = "1")
            @PathVariable Long userId) {

        User currentUser = userDetails.getUser();
        User userToFollow = userService.findUserById(userId);
        followerService.followUser(currentUser, userToFollow);

        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Dejar de seguir a un usuario",
            description = "Permite que el usuario autenticado deje de seguir a otro usuario."
    )
    @DeleteMapping("/{userId}/unfollow")
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
    @GetMapping("/{userId}/is-following")
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
