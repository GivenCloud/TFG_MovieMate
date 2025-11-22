package com.moviemate.repository;

import com.moviemate.entity.List;
import com.moviemate.entity.Content;
import com.moviemate.entity.ListContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ListContentRepository extends JpaRepository<ListContent, Long> {
    Optional<ListContent> findByListAndContent(List list, Content content);
    boolean existsByListAndContent(List list, Content content);
    
    @Query("SELECT COUNT(lc) FROM ListContent lc WHERE lc.list.id = :listId")
    Integer countByListId(@Param("listId") Long listId);
}