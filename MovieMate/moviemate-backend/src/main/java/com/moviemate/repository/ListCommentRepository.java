package com.moviemate.repository;

import com.moviemate.entity.ListComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListCommentRepository extends JpaRepository<ListComment, Long> {
    List<ListComment> findByListIdAndDeletedFalseOrderByCreatedAtAsc(Long listId);
}
