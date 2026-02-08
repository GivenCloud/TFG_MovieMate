package com.moviemate.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moviemate.entity.FollowRequest;
import com.moviemate.entity.User;

public interface FollowRequestRepository extends JpaRepository<FollowRequest, Long> {

    boolean existsBySenderAndReceiverAndStatus(User sender, User receiver, FollowRequest.FollowRequestStatus status);

    Optional<FollowRequest> findBySenderAndReceiverAndStatus(User sender, User receiver, FollowRequest.FollowRequestStatus status);

    Optional<FollowRequest> findBySenderAndReceiver(User sender, User receiver);

    List<FollowRequest> findByReceiver(User receiver);
}

