package com.moviemate.repository;

import com.moviemate.entity.Rating;
import com.moviemate.entity.User;
import com.moviemate.entity.Content;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByUserAndContent(User user, Content content);
    List<Rating> findAllByUserAndContent(User user, Content content);
    List<Rating> findByUser(User user);
    List<Rating> findByContent(Content content);
    boolean existsByUserAndContent(User user, Content content);
    
    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.content.id = :contentId")
    Double calculateAverageRatingByContent(@Param("contentId") Long contentId);
    
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.content.id = :contentId")
    Integer countRatingsByContent(@Param("contentId") Long contentId);

    @Query("SELECT r FROM Rating r WHERE r.user IN :users ORDER BY r.createdAt DESC")
    Page<Rating> findByUserInOrderByCreatedAtDesc(@Param("users") List<User> users, Pageable pageable);
    
    @Query("SELECT r FROM Rating r ORDER BY r.createdAt DESC")
    Page<Rating> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // ── Estadísticas avanzadas ───────────────────────────────────

    /** Distribución de notas: [rating, count] agrupado por valor (1-5) */
    @Query("SELECT r.rating, COUNT(r) FROM Rating r WHERE r.user = :user GROUP BY r.rating ORDER BY r.rating")
    List<Object[]> countRatingsByRatingValue(@Param("user") User user);

    /** Top géneros: [genre, count] de los contenidos valorados por el usuario */
    @Query("SELECT g, COUNT(r) FROM Rating r JOIN r.content c JOIN c.genres g WHERE r.user = :user GROUP BY g ORDER BY COUNT(r) DESC")
    List<Object[]> findTopGenresByUser(@Param("user") User user, Pageable pageable);

    /** Actividad mensual: [year, month, count] de los últimos N meses */
    @Query(value = """
            SELECT EXTRACT(YEAR FROM created_at)  AS year,
                   EXTRACT(MONTH FROM created_at) AS month,
                   COUNT(*)                        AS count
              FROM ratings
             WHERE user_id = :userId
               AND created_at >= :since
             GROUP BY 1, 2
             ORDER BY 1, 2
            """, nativeQuery = true)
    List<Object[]> findMonthlyActivity(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}