package com.moviemate.controller;

import com.moviemate.dto.ListResponse;
import com.moviemate.dto.RatingResponse;
import com.moviemate.dto.UpdateProfileRequest;
import com.moviemate.dto.UserProfileResponse;
import com.moviemate.dto.UserResponse;
import com.moviemate.dto.UserStatsResponse;
import com.moviemate.entity.User;
import com.moviemate.security.CustomUserDetails;
import com.moviemate.service.FollowerService;
import com.moviemate.service.ListService;
import com.moviemate.service.RatingService;
import com.moviemate.service.UserService;
import com.moviemate.service.UserStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
    private final UserStatsService userStatsService;
    private final ListService listService;
    private final RatingService ratingService;

    @Operation(summary = "Obtener el usuario actual")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        UserResponse userResponse = userService.mapToUserResponse(user);
        return ResponseEntity.ok(userResponse);
    }

    @Operation(summary = "Obtener un usuario por nombre de usuario")
    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(
            @Parameter(description = "Nombre de usuario") @PathVariable String username) {
        User user = userService.findUserByUsername(username);
        UserResponse userResponse = userService.mapToUserResponse(user);
        return ResponseEntity.ok(userResponse);
    }

    @Operation(summary = "Obtener un usuario por ID")
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getUserProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "ID del usuario") @PathVariable Long userId) {
        User targetUser = userService.findUserById(userId);
        User currentUser = userDetails.getUser();

        UserProfileResponse profile = new UserProfileResponse();
        profile.setId(targetUser.getId());
        profile.setUsername(targetUser.getUsername());
        profile.setEmail(targetUser.getEmail());
        profile.setAvatarUrl(targetUser.getAvatarUrl());
        profile.setBio(targetUser.getBio());
        profile.setCreatedAt(targetUser.getCreatedAt());
        profile.setFollowersCount(followerService.getFollowersCount(targetUser));
        profile.setFollowingCount(followerService.getFollowingCount(targetUser));
        profile.setIsFollowing(followerService.isFollowing(currentUser, targetUser));

        return ResponseEntity.ok(profile);
    }

    @Operation(
        summary = "Actualizar perfil de usuario",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos para actualizar el perfil",
            required = true,
            content = @Content(
                schema = @Schema(implementation = UpdateProfileRequest.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = "{\"bio\":\"Nueva bio\",\"avatarUrl\":\"https://miavatar.com/avatar.png\"}"
                )
            )
        )
    )
    @PutMapping("/me/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @org.springframework.web.bind.annotation.RequestBody UpdateProfileRequest request) {

        User user = userDetails.getUser();
        UserResponse updatedUser = userService.updateUserProfile(user, request.getBio(), request.getAvatarUrl());
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Buscar usuarios por query")
    @GetMapping
    public ResponseEntity<List<UserResponse>> searchUsers(
            @Parameter(description = "Query de búsqueda") @RequestParam String q) {
        List<UserResponse> users = userService.searchUsers(q);
        return ResponseEntity.ok(users);
    }

    @Operation(
            summary = "Obtener las listas del usuario autenticado",
            description = "Devuelve todas las listas creadas por el usuario."
    )
    @GetMapping("/me/lists")
    public ResponseEntity<List<ListResponse>> getUserLists(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        return ResponseEntity.ok(listService.getUserLists(user));
    }

    @Operation(summary = "Obtener todas las puntuaciones del usuario actual")
    @GetMapping("/me/ratings")
    public ResponseEntity<List<RatingResponse>> getUserRatings(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        return ResponseEntity.ok(ratingService.getUserRatings(user));
    }

    @Operation(summary = "Obtener usuarios sugeridos")
    @GetMapping("/suggestions")
    public ResponseEntity<List<UserResponse>> getSuggestedUsers(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User currentUser = userDetails.getUser();
        List<UserResponse> suggestedUsers = userService.getSuggestedUsers(currentUser);
        return ResponseEntity.ok(suggestedUsers);
    }

    @Operation(summary = "Obtener estadísticas de un usuario")
    @GetMapping("/{userId}/stats")
    public ResponseEntity<UserStatsResponse> getUserStats(
            @Parameter(description = "ID del usuario") @PathVariable Long userId) {
        UserStatsResponse stats = userStatsService.getOrCreateAndUpdateStats(userId);
        return ResponseEntity.ok(stats);
    }
}
