package com.moviemate.repository;

import com.moviemate.entity.ReviewLike;
import com.moviemate.entity.User;
import com.moviemate.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    Optional<ReviewLike> findByUserAndRating(User user, Rating rating);
    boolean existsByUserAndRating(User user, Rating rating);
    
    @Query("SELECT COUNT(rl) FROM ReviewLike rl WHERE rl.rating = :rating")
    Integer countByRating(@Param("rating") Rating rating);
    
    @Query("SELECT COUNT(rl) FROM ReviewLike rl WHERE rl.rating IN :ratings")
    Integer countByRatings(@Param("ratings") List<Rating> ratings);
    
    @Query("SELECT COUNT(rl) FROM ReviewLike rl WHERE rl.user = :user")
    Integer countByUser(User user);
}