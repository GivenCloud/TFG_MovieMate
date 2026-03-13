package com.moviemate.repository;

import com.moviemate.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByRatingIdAndDeletedFalseOrderByCreatedAtAsc(Long ratingId);
}
