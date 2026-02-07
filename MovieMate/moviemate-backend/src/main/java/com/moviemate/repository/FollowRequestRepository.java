package com.moviemate.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moviemate.entity.FollowRequest;
import com.moviemate.entity.User;

public interface FollowRequestRepository extends JpaRepository<FollowRequest, Long> {

    boolean existsBySenderAndReceiver(User sender, User receiver);

    Optional<FollowRequest> findBySenderAndReceiver(User sender, User receiver);

    List<FollowRequest> findByReceiver(User receiver);
}

