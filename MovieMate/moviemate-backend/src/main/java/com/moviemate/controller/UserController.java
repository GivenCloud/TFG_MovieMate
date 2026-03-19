package com.moviemate.controller;

import com.moviemate.annotation.RequirePublicProfile;
import com.moviemate.dto.BadgeDto;
import com.moviemate.dto.ChangePasswordRequest;
import com.moviemate.dto.FullStatsDto;
import com.moviemate.dto.FollowRequestDto;
import com.moviemate.dto.ListResponse;
import com.moviemate.dto.NotificationDto;
import com.moviemate.dto.RatingResponse;
import com.moviemate.dto.UpdateProfileRequest;
import com.moviemate.dto.UpdateUserPublicStatusRequest;
import com.moviemate.dto.UserProfileResponse;
import com.moviemate.dto.UserResponse;
import com.moviemate.dto.UserStatsResponse;
import com.moviemate.entity.User;
import com.moviemate.security.CustomUserDetails;
import com.moviemate.service.BadgeService;
import com.moviemate.service.ContentService;
import com.moviemate.service.FollowRequestService;
import com.moviemate.service.FollowerService;
import com.moviemate.service.ListService;
import com.moviemate.service.NotificationService;
import com.moviemate.service.RatingService;
import com.moviemate.service.TmdbService;
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

import org.springframework.web.multipart.MultipartFile;

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
    private final FollowRequestService followRequestService;
    private final NotificationService notificationService;
    private final TmdbService tmdbService;
    private final ContentService contentService;
    private final BadgeService badgeService;

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
    @RequirePublicProfile(userId = "#userId")
    public ResponseEntity<UserProfileResponse> getUserProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "ID del usuario") @PathVariable Long userId) {
        User targetUser = userService.findUserById(userId);
        User currentUser = userDetails != null ? userDetails.getUser() : null;

        UserProfileResponse profile = new UserProfileResponse();
        profile.setId(targetUser.getId());
        profile.setUsername(targetUser.getUsername());
        profile.setEmail(targetUser.getEmail());
        profile.setAvatarUrl(targetUser.getAvatarUrl());
        profile.setBio(targetUser.getBio());
        profile.setIsPublic(targetUser.getIsPublic());
        profile.setCreatedAt(targetUser.getCreatedAt());
        profile.setFollowersCount(followerService.getFollowersCount(targetUser));
        profile.setFollowingCount(followerService.getFollowingCount(targetUser));
        profile.setIsFollowing(currentUser != null && followerService.isFollowing(currentUser, targetUser));

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

    @Operation(
        summary = "Actualizar visibilidad del perfil",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos para actualizar la visibilidad del perfil",
            required = true,
            content = @Content(
                schema = @Schema(implementation = UpdateUserPublicStatusRequest.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = "{\"isPublic\":false}"
                )
            )
        )
    )
    @PutMapping("/me/public-status")
    public ResponseEntity<UserResponse> updatePublicStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @org.springframework.web.bind.annotation.RequestBody UpdateUserPublicStatusRequest request) {

        User user = userDetails.getUser();
        UserResponse updatedUser = userService.updateUserPublicStatus(user, request.getIsPublic());
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Subir avatar del usuario actual (multipart)")
    @PostMapping(value = "/me/avatar", consumes = "multipart/form-data")
    public ResponseEntity<UserResponse> uploadAvatar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        try {
            User user = userDetails.getUser();
            return ResponseEntity.ok(userService.uploadAvatar(user, file));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Cambiar contraseña del usuario actual")
    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @org.springframework.web.bind.annotation.RequestBody @jakarta.validation.Valid ChangePasswordRequest request) {
        User user = userDetails.getUser();
        userService.changePassword(user, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Buscar usuarios por query")
    @GetMapping
    public ResponseEntity<List<UserResponse>> searchUsers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Query de búsqueda") @RequestParam String q) {
        Long currentUserId = userDetails != null ? userDetails.getUser().getId() : null;
        List<UserResponse> users = userService.searchUsers(currentUserId, q);
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

    @GetMapping("/me/follow-requests")
    public ResponseEntity<List<FollowRequestDto>> getMyRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        List<FollowRequestDto> requests = followRequestService.findByReceiver(user);

        return ResponseEntity.ok(requests);
    }

    @GetMapping("/me/notifications")
    public ResponseEntity<List<NotificationDto>> getMyNotifications(@AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = userDetails.getUser();
        List<NotificationDto> notifications = notificationService.getNotifications(user);

        return ResponseEntity.ok(notifications);
    }


    @Operation(summary = "Obtener usuarios sugeridos")
    @GetMapping("/suggestions")
    public ResponseEntity<List<UserResponse>> getSuggestedUsers(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User currentUser = userDetails.getUser();
        List<UserResponse> suggestedUsers = userService.getSuggestedUsers(currentUser.getId());
        return ResponseEntity.ok(suggestedUsers);
    }

    @Operation(summary = "Obtener recomendaciones personalizadas basadas en géneros favoritos")
    @GetMapping("/me/recommendations")
    public ResponseEntity<java.util.List<com.moviemate.dto.ContentResponse>> getMyRecommendations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        // Obtener top géneros del usuario
        com.moviemate.dto.FullStatsDto stats = userStatsService.getFullStats(user);
        java.util.List<com.moviemate.dto.ContentResponse> recommendations = new java.util.ArrayList<>();

        if (!stats.getTopGenres().isEmpty()) {
            // Buscar géneros en TMDB para películas
            java.util.List<com.moviemate.dto.GenreDto> movieGenres = tmdbService.getMovieGenres();
            java.util.List<com.moviemate.dto.GenreDto> tvGenres    = tmdbService.getTvGenres();

            // Tomar el top-1 género como filtro de discover
            String topGenreName = stats.getTopGenres().get(0).getGenre();

            java.util.Optional<com.moviemate.dto.GenreDto> movieGenre = movieGenres.stream()
                    .filter(g -> g.getName().equalsIgnoreCase(topGenreName)).findFirst();
            java.util.Optional<com.moviemate.dto.GenreDto> tvGenre = tvGenres.stream()
                    .filter(g -> g.getName().equalsIgnoreCase(topGenreName)).findFirst();

            if (movieGenre.isPresent()) {
                tmdbService.discoverMovies(movieGenre.get().getId(), null, 7.0, "vote_average.desc", 1)
                        .stream().limit(6)
                        .map(contentService::mapToContentResponse)
                        .forEach(recommendations::add);
            }
            if (tvGenre.isPresent()) {
                tmdbService.discoverTvShows(tvGenre.get().getId(), null, 7.0, "vote_average.desc", 1)
                        .stream().limit(6)
                        .map(contentService::mapToContentResponse)
                        .forEach(recommendations::add);
            }
        }
        return ResponseEntity.ok(recommendations);
    }

    @Operation(summary = "Obtener estadísticas completas del usuario autenticado")
    @GetMapping("/me/stats/full")
    public ResponseEntity<FullStatsDto> getMyFullStats(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        return ResponseEntity.ok(userStatsService.getFullStats(user));
    }

    @Operation(summary = "Obtener estadísticas de un usuario")
    @GetMapping("/{userId}/stats")
    @RequirePublicProfile(userId = "#userId")
    public ResponseEntity<UserStatsResponse> getUserStats(
            @Parameter(description = "ID del usuario") @PathVariable Long userId) {
        UserStatsResponse stats = userStatsService.getOrCreateAndUpdateStats(userId);
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Obtener valoraciones públicas de un usuario")
    @GetMapping("/{userId}/ratings")
    @RequirePublicProfile(userId = "#userId")
    public ResponseEntity<List<RatingResponse>> getUserRatingsById(
            @Parameter(description = "ID del usuario") @PathVariable Long userId) {
        return ResponseEntity.ok(ratingService.getUserRatingsByUserId(userId));
    }

    @Operation(summary = "Obtener listas públicas de un usuario")
    @GetMapping("/{userId}/lists")
    @RequirePublicProfile(userId = "#userId")
    public ResponseEntity<List<ListResponse>> getUserListsById(
            @Parameter(description = "ID del usuario") @PathVariable Long userId) {
        return ResponseEntity.ok(listService.getListsByUserId(userId));
    }

    @Operation(summary = "Obtener insignias del usuario autenticado")
    @GetMapping("/me/badges")
    public ResponseEntity<List<BadgeDto>> getMyBadges(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        return ResponseEntity.ok(badgeService.getUserBadges(user));
    }

    @Operation(summary = "Obtener insignias de un usuario por ID")
    @GetMapping("/{userId}/badges")
    public ResponseEntity<List<BadgeDto>> getUserBadges(
            @PathVariable Long userId) {
        User user = userService.findUserById(userId);
        return ResponseEntity.ok(badgeService.getUserBadges(user));
    }
}
