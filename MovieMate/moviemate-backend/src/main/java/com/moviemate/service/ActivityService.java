package com.moviemate.service;

import com.moviemate.dto.ActivityResponse;
import com.moviemate.entity.User;
import com.moviemate.repository.FollowerRepository;
import com.moviemate.repository.RatingRepository;
import com.moviemate.repository.ListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
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
    
    @Transactional(readOnly = true)
    public List<ActivityResponse> getFeed(User user) {
        // Obtener los usuarios que sigue
        List<User> following = followerRepository.findByFollower(user).stream()
            .map(follower -> follower.getFollowed())
            .collect(Collectors.toList());
        
        List<ActivityResponse> activities = new ArrayList<>();
        
        // Actividades de valoraciones
        following.forEach(followedUser -> {
            ratingRepository.findByUser(followedUser).forEach(rating -> {
                activities.add(ActivityResponse.builder()
                    .type("RATING")
                    .user(userService.mapToUserResponse(followedUser))
                    .createdAt(rating.getCreatedAt())
                    .rating(ratingService.mapToRatingResponse(rating))
                    .build());
            });
        });
        
        // Actividades de creación de listas
        following.forEach(followedUser -> {
            listRepository.findByUser(followedUser).forEach(list -> {
                activities.add(ActivityResponse.builder()
                    .type("LIST_CREATION")
                    .user(userService.mapToUserResponse(followedUser))
                    .createdAt(list.getCreatedAt())
                    .list(listService.mapToListResponse(list))
                    .build());
            });
        });
        
        // Ordenar por fecha (más reciente primero)
        activities.sort((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()));
        
        return activities;
    }
}