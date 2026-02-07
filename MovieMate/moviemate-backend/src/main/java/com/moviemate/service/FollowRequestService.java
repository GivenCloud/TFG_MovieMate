package com.moviemate.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.moviemate.dto.FollowRequestActionResponse;
import com.moviemate.dto.FollowRequestDto;
import com.moviemate.dto.FollowRequestActionResponse.FollowRequestStatus;
import com.moviemate.entity.FollowRequest;
import com.moviemate.entity.Follower;
import com.moviemate.entity.User;
import com.moviemate.repository.FollowRequestRepository;
import com.moviemate.repository.FollowerRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FollowRequestService {

    private final FollowRequestRepository followRequestRepository;
    private final FollowerRepository followerRepository;

    @Transactional
    public java.util.List<FollowRequestDto> findByReceiver(User receiver) {
        return followRequestRepository.findByReceiver(receiver)
            .stream()
            .map(FollowRequestDto::from)
            .toList();
    }

    @Transactional
    public void sendFollowRequest(User sender, User receiver) {

        if (sender.getId().equals(receiver.getId())) {
            throw new RuntimeException("No puedes seguirte a ti mismo");
        }

        if (followerRepository.existsByFollowerAndFollowed(sender, receiver)) {
            throw new RuntimeException("Ya sigues a este usuario");
        }

        if (receiver.getIsPublic()) {
            Follower follower = new Follower();
            follower.setFollower(sender);
            follower.setFollowed(receiver);
            followerRepository.save(follower);
        } else {
            if (followRequestRepository.existsBySenderAndReceiver(sender, receiver)) {
                throw new RuntimeException("Ya has enviado una solicitud");
            }

            FollowRequest request = new FollowRequest();
            request.setSender(sender);
            request.setReceiver(receiver);

            followRequestRepository.save(request);
        }
    }

    @Transactional
    public FollowRequestActionResponse acceptRequest(Long requestId, User currentUser) {

        FollowRequest request = followRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (!request.getReceiver().getId().equals(currentUser.getId())) {
            throw new RuntimeException("No puedes aceptar esta solicitud");
        }

        // Crear follower
        Follower follower = new Follower();
        follower.setFollower(request.getSender());
        follower.setFollowed(request.getReceiver());
        followerRepository.save(follower);

        // Eliminar solicitud
        followRequestRepository.delete(request);

        return new FollowRequestActionResponse(
            request.getId(),
            request.getSender().getId(),
            request.getReceiver().getId(),
            FollowRequestStatus.ACCEPTED,
            LocalDateTime.now()
        );
    }

    @Transactional
    public FollowRequestActionResponse rejectRequest(Long requestId, User currentUser) {

        FollowRequest request = followRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (!request.getReceiver().getId().equals(currentUser.getId())) {
            throw new RuntimeException("No puedes rechazar esta solicitud");
        }

        followRequestRepository.delete(request);

        return new FollowRequestActionResponse(
            request.getId(),
            request.getSender().getId(),
            request.getReceiver().getId(),
            FollowRequestStatus.REJECTED,
            LocalDateTime.now()
        );
    }
}

