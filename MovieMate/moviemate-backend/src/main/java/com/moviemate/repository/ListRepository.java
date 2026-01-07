package com.moviemate.repository;

import com.moviemate.entity.List;
import com.moviemate.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ListRepository extends JpaRepository<List, Long> {
    
    @Query("SELECT DISTINCT l FROM List l LEFT JOIN FETCH l.contents lc LEFT JOIN FETCH lc.content WHERE l.user = :user")
    java.util.List<List> findByUserWithContents(@Param("user") User user);
    
    @Query("SELECT DISTINCT l FROM List l LEFT JOIN FETCH l.contents lc LEFT JOIN FETCH lc.content WHERE l.isPublic = true ORDER BY l.createdAt DESC")
    java.util.List<List> findPublicListsWithContents();
    
    java.util.List<List> findByUser(User user);
    java.util.List<List> findByUserAndIsPublic(User user, Boolean isPublic);
    Optional<List> findByUserAndListType(User user, List.ListType listType);
    
    boolean existsByUserAndName(User user, String name);

    @Query("SELECT l FROM List l WHERE l.isPublic = true ORDER BY l.createdAt DESC")
    java.util.List<List> findPublicLists();

    Page<List> findByUserInAndIsPublicTrueOrderByCreatedAtDesc(
        java.util.List<User> users,
        Pageable pageable
    );

    Page<List> findByIsPublicTrueOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT COUNT(l) FROM List l WHERE l.user = :user")
    Integer countByUser(@Param("user") User user);
}