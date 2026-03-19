package com.moviemate.service;

import com.moviemate.dto.BadgeDto;
import com.moviemate.entity.User;
import com.moviemate.entity.UserBadge;
import com.moviemate.entity.UserStats;
import com.moviemate.repository.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final UserBadgeRepository userBadgeRepository;

    // ── Catálogo de insignias ──────────────────────────────────────────────
    public enum BadgeType {
        FIRST_REVIEW,
        CRITIC,
        CINEPHILE,
        FILM_BUFF,
        MOVIE_MARATHON,
        SERIES_BINGE,
        SOCIAL,
        POPULAR,
        LISTER,
        LIKED
    }

    private static final Map<String, BadgeType> BY_CODE = java.util.Arrays.stream(BadgeType.values())
            .collect(Collectors.toMap(Enum::name, b -> b));

    // ── Obtener insignias de un usuario ───────────────────────────────────
    public List<BadgeDto> getUserBadges(User user) {
        return userBadgeRepository.findByUser(user).stream()
                .map(ub -> toDto(ub, BY_CODE.get(ub.getBadgeType())))
                .collect(Collectors.toList());
    }

    // ── Evaluar y otorgar nuevas insignias ────────────────────────────────
    @Transactional
    public void checkAndAward(User user, UserStats stats) {
        int ratings   = stats.getTotalRatings()   != null ? stats.getTotalRatings()   : 0;
        int movies    = stats.getMoviesWatched()  != null ? stats.getMoviesWatched()  : 0;
        int series    = stats.getSeriesWatched()  != null ? stats.getSeriesWatched()  : 0;
        int followers = stats.getFollowersCount() != null ? stats.getFollowersCount() : 0;
        int lists     = stats.getListsCreated()   != null ? stats.getListsCreated()   : 0;
        int likes     = stats.getLikesReceived()  != null ? stats.getLikesReceived()  : 0;

        award(user, BadgeType.FIRST_REVIEW,   ratings   >= 1);
        award(user, BadgeType.CRITIC,         ratings   >= 10);
        award(user, BadgeType.CINEPHILE,      ratings   >= 50);
        award(user, BadgeType.FILM_BUFF,      ratings   >= 100);
        award(user, BadgeType.MOVIE_MARATHON, movies    >= 10);
        award(user, BadgeType.SERIES_BINGE,   series    >= 10);
        award(user, BadgeType.SOCIAL,         followers >= 1);
        award(user, BadgeType.POPULAR,        followers >= 10);
        award(user, BadgeType.LISTER,         lists     >= 1);
        award(user, BadgeType.LIKED,          likes     >= 1);
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private void award(User user, BadgeType type, boolean condition) {
        if (!condition) return;
        if (userBadgeRepository.existsByUserAndBadgeType(user, type.name())) return;
        UserBadge badge = new UserBadge();
        badge.setUser(user);
        badge.setBadgeType(type.name());
        userBadgeRepository.save(badge);
    }

    private BadgeDto toDto(UserBadge ub, BadgeType type) {
        BadgeDto dto = new BadgeDto();
        dto.setType(ub.getBadgeType());
        dto.setAwardedAt(ub.getAwardedAt());
        if (type != null) {
            dto.setName(getName(type));
            dto.setDescription(getDescription(type));
            dto.setIcon(getIcon(type));
        } else {
            dto.setName(ub.getBadgeType());
        }
        return dto;
    }

    private String getName(BadgeType t) {
        return switch (t) {
            case FIRST_REVIEW   -> "Primera valoración";
            case CRITIC         -> "Crítico";
            case CINEPHILE      -> "Cinéfilo";
            case FILM_BUFF      -> "Apasionado del cine";
            case MOVIE_MARATHON -> "Maratón de pelis";
            case SERIES_BINGE   -> "Binge-watcher";
            case SOCIAL         -> "Sociable";
            case POPULAR        -> "Popular";
            case LISTER         -> "Curador de listas";
            case LIKED          -> "Valorado";
        };
    }

    private String getDescription(BadgeType t) {
        return switch (t) {
            case FIRST_REVIEW   -> "Tu primera valoración. ¡Bienvenido al club!";
            case CRITIC         -> "10 valoraciones publicadas.";
            case CINEPHILE      -> "50 valoraciones publicadas.";
            case FILM_BUFF      -> "100 valoraciones publicadas.";
            case MOVIE_MARATHON -> "10 películas vistas.";
            case SERIES_BINGE   -> "10 series vistas.";
            case SOCIAL         -> "Tu primer seguidor.";
            case POPULAR        -> "10 seguidores.";
            case LISTER         -> "Primera lista creada.";
            case LIKED          -> "Tu primera valoración con like.";
        };
    }

    private String getIcon(BadgeType t) {
        return switch (t) {
            case FIRST_REVIEW   -> "⭐";
            case CRITIC         -> "🖊️";
            case CINEPHILE      -> "🎞️";
            case FILM_BUFF      -> "🏆";
            case MOVIE_MARATHON -> "🎬";
            case SERIES_BINGE   -> "📺";
            case SOCIAL         -> "👥";
            case POPULAR        -> "🌟";
            case LISTER         -> "📋";
            case LIKED          -> "❤️";
        };
    }
}
