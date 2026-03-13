package com.moviemate.config;

import com.moviemate.dto.ListRequest;
import com.moviemate.dto.ListResponse;
import com.moviemate.dto.RatingRequest;
import com.moviemate.entity.Content;
import com.moviemate.entity.Follower;
import com.moviemate.entity.List.ListType;
import com.moviemate.entity.Rating;
import com.moviemate.entity.Role;
import com.moviemate.entity.User;
import com.moviemate.repository.FollowerRepository;
import com.moviemate.repository.UserRepository;
import com.moviemate.service.ListService;
import com.moviemate.service.RatingService;
import com.moviemate.service.TmdbService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Puebla la base de datos con datos de prueba la primera vez que arranca
 * la aplicación (idempotente: no hace nada si ya hay usuarios).
 *
 * Usuarios creados: alice / bob / charlie  —  contraseña: Test1234!
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final UserRepository     userRepository;
    private final FollowerRepository followerRepository;
    private final ListService        listService;
    private final RatingService      ratingService;
    private final TmdbService        tmdbService;
    private final PasswordEncoder    passwordEncoder;

    // TMDB IDs — películas populares bien conocidas
    private static final int[] MOVIE_IDS = { 550, 27205, 157336, 603, 238 };
    // TMDB IDs — series populares bien conocidas
    private static final int[] TV_IDS    = { 1396, 1399, 66732 };

    @Override
    public void run(ApplicationArguments args) {
        // Crear usuario admin si no existe (se ejecuta siempre)
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@moviemate.com");
            admin.setPasswordHash(passwordEncoder.encode("Admin1234!"));
            admin.setBio("Administrador de MovieMate.");
            admin.setIsPublic(true);
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            log.info("DataSeeder: usuario admin creado (admin@moviemate.com / Admin1234!)");
        }

        if (userRepository.count() > 1) {
            log.info("DataSeeder: la BD ya contiene datos, omitiendo seed.");
            return;
        }

        log.info("DataSeeder: iniciando seed de datos de prueba...");

        try {
            // ── 1. Sincronizar contenido desde TMDB ─────────────────────────
            java.util.List<Content> movies  = syncContent(MOVIE_IDS, true);
            java.util.List<Content> tvShows = syncContent(TV_IDS,    false);

            // ── 2. Crear usuarios ────────────────────────────────────────────
            User alice   = createUser("alice",   "alice@moviemate.com",
                    "Cinéfila empedernida. Si no la he visto yo, no merece la pena.",
                    "https://i.pravatar.cc/150?u=alice");
            User bob     = createUser("bob",     "bob@moviemate.com",
                    "Fan de las series de culto y el cine de autor.",
                    "https://i.pravatar.cc/150?u=bob");
            User charlie = createUser("charlie", "charlie@moviemate.com",
                    "Solo veo pelis los fines de semana, pero las disfruto al máximo.",
                    null);

            // ── 3. Crear listas ──────────────────────────────────────────────
            ListResponse aliceFavs    = createList(alice,   "Mis Favoritos",  null,                                      ListType.FAVORITES, true);
            ListResponse aliceWatch   = createList(alice,   "Por Ver",        null,                                      ListType.WATCHLIST, true);
            ListResponse aliceWatched = createList(alice,   "Vistas",         null,                                      ListType.WATCHED,   true);
            ListResponse aliceCustom  = createList(alice,   "Obras maestras", "Lo mejor que he visto en mi vida.",       ListType.CUSTOM,    true);

            ListResponse bobFavs      = createList(bob,     "Mis Favoritos",  null,                                      ListType.FAVORITES, true);
            ListResponse bobWatch     = createList(bob,     "Por Ver",        null,                                      ListType.WATCHLIST, true);
            ListResponse bobWatched   = createList(bob,     "Vistas",         null,                                      ListType.WATCHED,   true);

            ListResponse charlieFavs     = createList(charlie, "Mis Favoritos", null, ListType.FAVORITES, false);
            ListResponse charlieWatch    = createList(charlie, "Por Ver",        null, ListType.WATCHLIST, false);
            ListResponse charlieWatched  = createList(charlie, "Vistas",         null, ListType.WATCHED,   false);

            // ── 4. Añadir contenido a listas ─────────────────────────────────
            if (!movies.isEmpty()) {
                int fightClub    = movies.get(0).getTmdbId(); // 550
                int inception    = movies.get(1).getTmdbId(); // 27205
                int interstellar = movies.get(2).getTmdbId(); // 157336
                int matrix       = movies.get(3).getTmdbId(); // 603
                int godfather    = movies.get(4).getTmdbId(); // 238

                addToList(alice, aliceFavs.getId(),    fightClub);
                addToList(alice, aliceFavs.getId(),    inception);
                addToList(alice, aliceWatch.getId(),   matrix);
                addToList(alice, aliceWatched.getId(), fightClub);
                addToList(alice, aliceWatched.getId(), inception);
                addToList(alice, aliceCustom.getId(),  fightClub);
                addToList(alice, aliceCustom.getId(),  interstellar);

                addToList(bob, bobFavs.getId(),    interstellar);
                addToList(bob, bobWatch.getId(),   godfather);
                addToList(bob, bobWatched.getId(), inception);
            }

            if (!tvShows.isEmpty()) {
                int breakingBad    = tvShows.get(0).getTmdbId(); // 1396
                int got            = tvShows.get(1).getTmdbId(); // 1399
                int strangerThings = tvShows.get(2).getTmdbId(); // 66732

                addToList(alice,   aliceFavs.getId(),      breakingBad);
                addToList(alice,   aliceWatch.getId(),     got);
                addToList(bob,     bobFavs.getId(),        breakingBad);
                addToList(charlie, charlieWatch.getId(),   strangerThings);
                addToList(charlie, charlieFavs.getId(),    breakingBad);
            }

            // ── 5. Crear valoraciones ────────────────────────────────────────
            if (!movies.isEmpty()) {
                int fightClub    = movies.get(0).getTmdbId();
                int inception    = movies.get(1).getTmdbId();
                int interstellar = movies.get(2).getTmdbId();
                int godfather    = movies.get(4).getTmdbId();

                createRating(alice, fightClub, 5,
                        "Una obra maestra. El giro final sigue impresionándome.",
                        Rating.EmotionalTag.INCREIBLE, Rating.Status.VISTA, LocalDate.of(2024, 3, 10));
                createRating(alice, inception, 5,
                        "El sueño dentro del sueño es pura magia cinematográfica.",
                        Rating.EmotionalTag.INCREIBLE, Rating.Status.VISTA, LocalDate.of(2024, 5, 20));
                createRating(alice, interstellar, 4,
                        "Visualmente impactante, aunque el final me dejó algo frío.",
                        Rating.EmotionalTag.RECOMENDADA, Rating.Status.VISTA, LocalDate.of(2023, 11, 15));
                createRating(alice, godfather, 5,
                        "El Padrino es intocable. Cine con mayúsculas.",
                        Rating.EmotionalTag.INCREIBLE, Rating.Status.VISTA, LocalDate.of(2024, 1, 5));

                createRating(bob, inception, 4,
                        "Gran película, aunque prefiero Memento de Nolan.",
                        Rating.EmotionalTag.RECOMENDADA, Rating.Status.VISTA, LocalDate.of(2024, 4, 14));
                createRating(bob, interstellar, 5,
                        "Interstellar me hizo llorar. Banda sonora increíble.",
                        Rating.EmotionalTag.INCREIBLE, Rating.Status.VISTA, LocalDate.of(2024, 6, 1));

                createRating(charlie, fightClub, 3,
                        "Me gustó, aunque esperaba más del final.",
                        Rating.EmotionalTag.ENTRETENIDA, Rating.Status.VISTA, LocalDate.of(2024, 7, 22));
            }

            if (!tvShows.isEmpty()) {
                int breakingBad = tvShows.get(0).getTmdbId();

                createRating(alice, breakingBad, 5,
                        "Breaking Bad es la mejor serie que he visto. Sin discusión.",
                        Rating.EmotionalTag.INCREIBLE, Rating.Status.VISTA, LocalDate.of(2023, 9, 3));
                createRating(bob, breakingBad, 5,
                        "La evolución de Walter White es una clase magistral de guion.",
                        Rating.EmotionalTag.INCREIBLE, Rating.Status.VISTA, LocalDate.of(2024, 2, 28));
            }

            // ── 6. Crear seguimientos ────────────────────────────────────────
            follow(alice,   bob);
            follow(alice,   charlie);
            follow(bob,     alice);
            follow(charlie, alice);

            log.info("DataSeeder: seed completado. Usuarios: alice, bob, charlie / Contraseña: Test1234!");

        } catch (Exception e) {
            log.error("DataSeeder: error durante el seed — {}", e.getMessage(), e);
        }
    }

    // ────── Helpers ─────────────────────────────────────────────────────────

    private java.util.List<Content> syncContent(int[] tmdbIds, boolean isMovie) {
        java.util.List<Content> result = new ArrayList<>();
        for (int id : tmdbIds) {
            try {
                Content c = isMovie
                        ? tmdbService.syncMovieFromTmdb(id)
                        : tmdbService.syncTvShowFromTmdb(id);
                if (c != null) {
                    result.add(c);
                    log.info("DataSeeder: sincronizado {} tmdbId={}  ({})",
                            isMovie ? "película" : "serie", id, c.getTitle());
                }
            } catch (Exception e) {
                log.warn("DataSeeder: no se pudo sincronizar tmdbId={} — {}", id, e.getMessage());
            }
        }
        return result;
    }

    private User createUser(String username, String email, String bio, String avatarUrl) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("Test1234!"));
        user.setBio(bio);
        user.setIsPublic(true);
        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
        }
        return userRepository.save(user);
    }

    private ListResponse createList(User user, String name, String description,
                                    ListType listType, boolean isPublic) {
        ListRequest req = new ListRequest();
        req.setName(name);
        req.setDescription(description);
        req.setIsPublic(isPublic);
        req.setListType(listType);
        return listService.createList(user, req);
    }

    private void addToList(User user, Long listId, int tmdbId) {
        try {
            listService.addContentToList(user, listId, tmdbId);
        } catch (Exception e) {
            log.warn("DataSeeder: no se pudo añadir tmdbId={} a lista id={} — {}",
                    tmdbId, listId, e.getMessage());
        }
    }

    private void createRating(User user, int tmdbId, int rating, String review,
                               Rating.EmotionalTag tag, Rating.Status status,
                               LocalDate watchedDate) {
        try {
            RatingRequest req = new RatingRequest();
            req.setTmdbId(tmdbId);
            req.setRating(rating);
            req.setReviewText(review);
            req.setEmotionalTag(tag);
            req.setStatus(status);
            req.setWatchedDate(watchedDate);
            ratingService.createOrUpdateRating(user, req);
        } catch (Exception e) {
            log.warn("DataSeeder: no se pudo crear valoración tmdbId={} user={} — {}",
                    tmdbId, user.getUsername(), e.getMessage());
        }
    }

    private void follow(User follower, User followed) {
        try {
            if (!followerRepository.existsByFollowerAndFollowed(follower, followed)) {
                Follower f = new Follower();
                f.setFollower(follower);
                f.setFollowed(followed);
                followerRepository.save(f);
            }
        } catch (Exception e) {
            log.warn("DataSeeder: no se pudo crear seguimiento {} → {} — {}",
                    follower.getUsername(), followed.getUsername(), e.getMessage());
        }
    }
}
