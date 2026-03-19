package com.moviemate.repository;

import com.moviemate.entity.User;
import com.moviemate.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    List<UserBadge> findByUser(User user);
    boolean existsByUserAndBadgeType(User user, String badgeType);
}
