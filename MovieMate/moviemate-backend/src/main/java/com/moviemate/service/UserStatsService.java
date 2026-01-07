package com.moviemate.service;

import com.moviemate.dto.UserStatsResponse;
import com.moviemate.entity.*;
import com.moviemate.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserStatsService {

    private final UserStatsRepository userStatsRepository;
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;
    private final ListRepository listRepository;
    private final FollowerRepository followerRepository;
    private final ReviewLikeRepository reviewLikeRepository;

    @Transactional
    public UserStatsResponse getOrCreateAndUpdateStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        UserStatsResponse updatedStats = updateUserStats(user);
        return updatedStats;
    }

    @Transactional
    public UserStatsResponse updateUserStats(User user) {
        UserStats stats = userStatsRepository.findByUser(user)
                .orElseGet(() -> createDefaultUserStats(user));

        // Obtener todas las valoraciones del usuario
        List<Rating> userRatings = ratingRepository.findByUser(user);

        // Calcular estadísticas
        stats.setTotalRatings(userRatings.size());
        stats.setAverageRating(calculateAverageRating(userRatings));
        
        // Contar películas y series vistas
        long moviesWatched = userRatings.stream()
                .filter(rating -> rating.getContent() != null && 
                                 rating.getContent().getContentType() == Content.ContentType.MOVIE &&
                                 rating.getStatus() == Rating.Status.VISTA)
                .count();
        stats.setMoviesWatched((int) moviesWatched);
        
        long seriesWatched = userRatings.stream()
                .filter(rating -> rating.getContent() != null && 
                                 rating.getContent().getContentType() == Content.ContentType.TV &&
                                 rating.getStatus() == Rating.Status.VISTA)
                .count();
        stats.setSeriesWatched((int) seriesWatched);

        // Contar listas creadas
        int listsCount = listRepository.countByUser(user);
        stats.setListsCreated(listsCount);

        // Contar seguidores y seguidos
        int followersCount = followerRepository.countByFollowed(user);
        int followingCount = followerRepository.countByFollower(user);
        stats.setFollowersCount(followersCount);
        stats.setFollowingCount(followingCount);

        // Contar likes recibidos en reseñas
        int likesReceived = userRatings.stream()
                .mapToInt(rating -> reviewLikeRepository.countByRating(rating))
                .sum();
        stats.setLikesReceived(likesReceived);

        // Calcular tiempo total visto (aproximado)
        // Películas: 120 min, Series: 45 min por episodio
        int totalWatchTime = (int) ((moviesWatched * 120) + (seriesWatched * 45));
        stats.setTotalWatchTime(totalWatchTime);

        return mapToUserStatsResponse(userStatsRepository.save(stats));
    }

    private double calculateAverageRating(List<Rating> ratings) {
        if (ratings.isEmpty()) {
            return 0.0;
        }
        
        return ratings.stream()
                .mapToInt(Rating::getRating)
                .average()
                .orElse(0.0);
    }

    private UserStats createDefaultUserStats(User user) {
        System.out.println(">>> createDefaultUserStats userId = " + user.getId());
        UserStats stats = new UserStats();
        stats.setUser(user);
        return stats;
    }

    @Transactional
    public void incrementListsCount(User user) {
        UserStats stats = userStatsRepository.findById(user.getId())
                .orElseGet(() -> createDefaultUserStats(user));
        stats.setListsCreated(stats.getListsCreated() + 1);
        userStatsRepository.save(stats);
    }

    @Transactional
    public void incrementRatingsCount(User user) {
        UserStats stats = userStatsRepository.findById(user.getId())
                .orElseGet(() -> createDefaultUserStats(user));
        stats.setTotalRatings(stats.getTotalRatings() + 1);

        List<Rating> ratings = ratingRepository.findByUser(user);
        stats.setAverageRating(calculateAverageRating(ratings));

        userStatsRepository.save(stats);
    }

    public UserStatsResponse mapToUserStatsResponse(UserStats stats) {
        return UserStatsResponse.builder()
                .totalRatings(stats.getTotalRatings())
                .averageRating(stats.getAverageRating())
                .moviesWatched(stats.getMoviesWatched())
                .seriesWatched(stats.getSeriesWatched())
                .totalWatchTime(stats.getTotalWatchTime())
                .listsCreated(stats.getListsCreated())
                .followersCount(stats.getFollowersCount())
                .followingCount(stats.getFollowingCount())
                .likesReceived(stats.getLikesReceived())
                .build();
    }

}