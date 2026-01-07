package com.moviemate.repository;

import com.moviemate.entity.User;
import com.moviemate.entity.UserStats;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStatsRepository extends JpaRepository<UserStats, Long> {
    Optional<UserStats> findByUser(User user);
}