// com.moviemate.service.ActivityService.java
package com.moviemate.service;

import com.moviemate.dto.ActivityResponse;
import com.moviemate.entity.*;
import com.moviemate.entity.List;
import com.moviemate.repository.FollowerRepository;
import com.moviemate.repository.RatingRepository;
import com.moviemate.repository.ListRepository;
import com.moviemate.dto.ContentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final FollowerRepository followerRepository;
    private final RatingRepository ratingRepository;
    private final ListRepository listRepository;
    private final RatingService ratingService;
    private final ListService listService;
    private final UserService userService;
    private final ContentService contentService;

    @Transactional(readOnly = true)
    public Page<ActivityResponse> getUserFeed(User user, Pageable pageable) {
        // Obtener usuarios que el usuario actual sigue
        java.util.List<User> followedUsers = followerRepository.findByFollower(user)
                .stream()
                .map(Follower::getFollowed)
                .collect(Collectors.toList());

        if (followedUsers.isEmpty()) {
                System.out.println("El usuario no sigue a nadie.");
                return Page.empty(pageable);
        }

        System.out.println("Usuarios seguidos: " + followedUsers.size());

        // Obtener actividades de los usuarios seguidos
       java.util.List<ActivityResponse> allActivities = new ArrayList<>();

        // Actividades de valoraciones
        Page<Rating> recentRatings = ratingRepository.findByUserInOrderByCreatedAtDesc(
                followedUsers, 
                PageRequest.of(0, 50)
        );
        
        allActivities.addAll(recentRatings.stream()
                .map(rating -> {
                    boolean updated = rating.getUpdatedAt() != null &&
                            rating.getCreatedAt() != null &&
                            rating.getUpdatedAt().isAfter(rating.getCreatedAt().plusMinutes(1));
                    return ActivityResponse.builder()
                            .type(updated ? ActivityType.RATING_UPDATED : ActivityType.RATING_CREATED)
                            .user(userService.mapToUserResponse(rating.getUser()))
                            .rating(ratingService.mapToRatingResponse(rating))
                            .content(contentService.mapToContentResponse(rating.getContent()))
                            .createdAt(updated ? rating.getUpdatedAt() : rating.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList()));

        // Actividades de listas creadas/actualizadas
        Page<List> recentLists = listRepository.findByUserInAndIsPublicTrueOrderByCreatedAtDesc(
                followedUsers,
                PageRequest.of(0, 30)
        );

        allActivities.addAll(recentLists.stream()
                .map(list -> {
                    boolean updated = list.getUpdatedAt() != null &&
                            list.getCreatedAt() != null &&
                            list.getUpdatedAt().isAfter(list.getCreatedAt().plusMinutes(1));
                    return ActivityResponse.builder()
                            .type(updated ? ActivityType.LIST_UPDATED : ActivityType.LIST_CREATED)
                            .user(userService.mapToUserResponse(list.getUser()))
                            .list(listService.mapToListResponse(list))
                            .createdAt(updated ? list.getUpdatedAt() : list.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList()));

        // Actividades de seguimientos (solo seguimientos recientes)
        Page<Follower> recentFollows = followerRepository.findByFollowerInAndCreatedAtAfterOrderByCreatedAtDesc(
                followedUsers,
                LocalDateTime.now().minusDays(7),
                PageRequest.of(0, 20)
        );
        
        allActivities.addAll(recentFollows.stream()
                .map(follow -> ActivityResponse.builder()
                        .type(ActivityType.FOLLOW)
                        .user(userService.mapToUserResponse(follow.getFollower()))
                        .targetUser(userService.mapToUserResponse(follow.getFollowed()))
                        .createdAt(follow.getCreatedAt())
                        .build())
                .collect(Collectors.toList()));

        // Ordenar por fecha descendente (null-safe)
        allActivities.sort(Comparator.comparing(ActivityResponse::getCreatedAt,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        // Paginar
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allActivities.size());

        if (start > allActivities.size()) {
            return Page.empty(pageable);
        }

        return new PageImpl<>(
                allActivities.subList(start, end),
                pageable,
                allActivities.size()
        );
    }

    @Transactional(readOnly = true)
    public Page<ActivityResponse> getGlobalActivity(Pageable pageable) {
        // Actividades recientes públicas de todos los usuarios
        java.util.List<ActivityResponse> allActivities = new ArrayList<>();

        // Valoraciones recientes
        Page<Rating> recentRatings = ratingRepository.findAllByOrderByCreatedAtDesc(pageable);
        allActivities.addAll(recentRatings.stream()
                .map(rating -> {
                    boolean updated = rating.getUpdatedAt() != null &&
                            rating.getCreatedAt() != null &&
                            rating.getUpdatedAt().isAfter(rating.getCreatedAt().plusMinutes(1));
                    return ActivityResponse.builder()
                            .type(updated ? ActivityType.RATING_UPDATED : ActivityType.RATING_CREATED)
                            .user(userService.mapToUserResponse(rating.getUser()))
                            .rating(ratingService.mapToRatingResponse(rating))
                            .content(contentService.mapToContentResponse(rating.getContent()))
                            .createdAt(updated ? rating.getUpdatedAt() : rating.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList()));

        // Listas públicas recientes
        Page<List> recentLists = listRepository.findByIsPublicTrueOrderByCreatedAtDesc(pageable);
        allActivities.addAll(recentLists.stream()
                .map(list -> {
                    boolean updated = list.getUpdatedAt() != null &&
                            list.getCreatedAt() != null &&
                            list.getUpdatedAt().isAfter(list.getCreatedAt().plusMinutes(1));
                    return ActivityResponse.builder()
                            .type(updated ? ActivityType.LIST_UPDATED : ActivityType.LIST_CREATED)
                            .user(userService.mapToUserResponse(list.getUser()))
                            .list(listService.mapToListResponse(list))
                            .createdAt(updated ? list.getUpdatedAt() : list.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList()));

        // Ordenar por fecha descendente (null-safe)
        allActivities.sort(Comparator.comparing(ActivityResponse::getCreatedAt,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        // Paginar
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allActivities.size());
        
        if (start > allActivities.size()) {
            return Page.empty(pageable);
        }

        return new PageImpl<>(
                allActivities.subList(start, end),
                pageable,
                allActivities.size()
        );
    }
}