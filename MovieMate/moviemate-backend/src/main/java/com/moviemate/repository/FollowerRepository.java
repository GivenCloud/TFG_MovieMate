package com.moviemate.repository;

import com.moviemate.entity.Follower;
import com.moviemate.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FollowerRepository extends JpaRepository<Follower, Long> {
    Optional<Follower> findByFollowerAndFollowed(User follower, User followed);
    List<Follower> findByFollower(User follower); // Usuarios que yo sigo
    List<Follower> findByFollowed(User followed); // Mis seguidores
    boolean existsByFollowerAndFollowed(User follower, User followed);
    
    @Query("SELECT COUNT(f) FROM Follower f WHERE f.followed.id = :userId")
    Integer countFollowersByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(f) FROM Follower f WHERE f.follower.id = :userId")
    Integer countFollowingByUserId(@Param("userId") Long userId);

    Page<Follower> findByFollowerInAndCreatedAtAfterOrderByCreatedAtDesc(
        List<User> followers,
        LocalDateTime createdAt,
        Pageable pageable
    );

    @Query("SELECT COUNT(f) FROM Follower f WHERE f.followed = :user")
    Integer countByFollowed(@Param("user") User user);

    @Query("SELECT COUNT(f) FROM Follower f WHERE f.follower = :user")
    Integer countByFollower(@Param("user") User user);
}