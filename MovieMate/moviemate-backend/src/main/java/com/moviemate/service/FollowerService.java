package com.moviemate.service;

import com.moviemate.dto.UserResponse;
import com.moviemate.entity.FollowRequest;
import com.moviemate.entity.Follower;
import com.moviemate.entity.User;
import com.moviemate.repository.FollowRequestRepository;
import com.moviemate.repository.FollowerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowerService {
    
    private final FollowerRepository followerRepository;
    private final FollowRequestRepository followRequestRepository;
    private final UserService userService;
    
    @Transactional
    public void unfollowUser(User followerUser, User userToUnfollow) {
        Follower follower = followerRepository.findByFollowerAndFollowed(followerUser, userToUnfollow)
            .orElseThrow(() -> new RuntimeException("No estás siguiendo a este usuario"));
        followerRepository.delete(follower);

        followRequestRepository
            .findBySenderAndReceiverAndStatus(
                followerUser, userToUnfollow, FollowRequest.FollowRequestStatus.ACCEPTED)
            .ifPresent(req -> req.setStatus(FollowRequest.FollowRequestStatus.CANCELLED));
    }
    
    @Transactional(readOnly = true)
    public List<UserResponse> getFollowers(Long userId) {
        User user = userService.findUserById(userId);
        return followerRepository.findByFollowed(user).stream()
            .map(f -> userService.mapToUserResponse(f.getFollower()))
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<UserResponse> getFollowing(Long userId) {
        User user = userService.findUserById(userId);
        return followerRepository.findByFollower(user).stream()
            .map(Follower::getFollowed)
            .map(userService::mapToUserResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public boolean isFollowing(User follower, User followed) {
        return followerRepository.existsByFollowerAndFollowed(follower, followed);
    }
    
    public Integer getFollowersCount(User user) {
        return followerRepository.countFollowersByUserId(user.getId());
    }
    
    public Integer getFollowingCount(User user) {
        return followerRepository.countFollowingByUserId(user.getId());
    }
}