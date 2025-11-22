package com.moviemate.repository;

import com.moviemate.entity.Rating;
import com.moviemate.entity.User;
import com.moviemate.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByUserAndContent(User user, Content content);
    List<Rating> findByUser(User user);
    List<Rating> findByContent(Content content);
    boolean existsByUserAndContent(User user, Content content);
    
    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.content.id = :contentId")
    Double calculateAverageRatingByContent(@Param("contentId") Long contentId);
    
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.content.id = :contentId")
    Integer countRatingsByContent(@Param("contentId") Long contentId);
}