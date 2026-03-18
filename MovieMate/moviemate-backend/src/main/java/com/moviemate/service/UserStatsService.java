package com.moviemate.service;

import com.moviemate.dto.FullStatsDto;
import com.moviemate.dto.UserStatsResponse;
import com.moviemate.entity.*;
import com.moviemate.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class UserStatsService {

    private final UserStatsRepository userStatsRepository;
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;
    private final ListRepository listRepository;
    private final FollowerRepository followerRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final BadgeService badgeService;

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

        UserStats saved = userStatsRepository.save(stats);
        badgeService.checkAndAward(user, saved);
        return mapToUserStatsResponse(saved);
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

    @Transactional
    public FullStatsDto getFullStats(User user) {
        // 1. Estadísticas básicas (reutiliza la lógica existente)
        UserStatsResponse basic = updateUserStats(user);

        // 2. Distribución de notas (1-5)
        List<Object[]> rawDist = ratingRepository.countRatingsByRatingValue(user);
        Map<Integer, Long> distMap = rawDist.stream().collect(
                Collectors.toMap(row -> ((Number) row[0]).intValue(), row -> ((Number) row[1]).longValue()));
        List<FullStatsDto.RatingCountDto> distribution = IntStream.rangeClosed(1, 5)
                .mapToObj(i -> new FullStatsDto.RatingCountDto(i, distMap.getOrDefault(i, 0L)))
                .collect(Collectors.toList());

        // 3. Top géneros (máx. 8)
        List<Object[]> rawGenres = ratingRepository.findTopGenresByUser(user, PageRequest.of(0, 8));
        List<FullStatsDto.GenreStatDto> topGenres = rawGenres.stream()
                .map(row -> new FullStatsDto.GenreStatDto((String) row[0], ((Number) row[1]).longValue()))
                .collect(Collectors.toList());

        // 4. Actividad mensual (últimos 12 meses)
        LocalDateTime since = LocalDateTime.now().minusMonths(12);
        List<Object[]> rawMonthly = ratingRepository.findMonthlyActivity(user.getId(), since);
        List<FullStatsDto.MonthlyActivityDto> monthly = rawMonthly.stream()
                .map(row -> new FullStatsDto.MonthlyActivityDto(
                        ((Number) row[0]).intValue(),
                        ((Number) row[1]).intValue(),
                        ((Number) row[2]).longValue()))
                .collect(Collectors.toList());

        FullStatsDto dto = new FullStatsDto();
        dto.setTotalRatings(basic.getTotalRatings());
        dto.setAverageRating(basic.getAverageRating());
        dto.setMoviesWatched(basic.getMoviesWatched());
        dto.setSeriesWatched(basic.getSeriesWatched());
        dto.setTotalWatchTime(basic.getTotalWatchTime());
        dto.setListsCreated(basic.getListsCreated());
        dto.setFollowersCount(basic.getFollowersCount());
        dto.setFollowingCount(basic.getFollowingCount());
        dto.setLikesReceived(basic.getLikesReceived());
        dto.setRatingDistribution(distribution);
        dto.setTopGenres(topGenres);
        dto.setMonthlyActivity(monthly);
        return dto;
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