package com.moviemate.service;

import com.moviemate.dto.FollowRequestActionResponse;
import com.moviemate.dto.FollowRequestDto;
import com.moviemate.entity.FollowRequest;
import com.moviemate.entity.Follower;
import com.moviemate.entity.Notification;
import com.moviemate.entity.User;
import com.moviemate.repository.FollowRequestRepository;
import com.moviemate.repository.FollowerRepository;
import com.moviemate.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FollowRequestServiceTest {

    private FollowRequestRepository followRequestRepository;
    private FollowerRepository followerRepository;
    private NotificationRepository notificationRepository;
    private NotificationService notificationService;
    private FollowRequestService followRequestService;

    @BeforeEach
    void setUp() {
        followRequestRepository = mock(FollowRequestRepository.class);
        followerRepository = mock(FollowerRepository.class);
        notificationRepository = mock(NotificationRepository.class);
        notificationService = mock(NotificationService.class);
        
        followRequestService = new FollowRequestService(
            followRequestRepository,
            followerRepository,
            notificationRepository,
            notificationService
        );
    }

    // ---------- findByReceiver ----------

    @Test
    void findByReceiver_shouldReturnAllRequests() {
        User receiver = buildUser(1L, "chris");
        User sender1 = buildUser(2L, "alex");
        User sender2 = buildUser(3L, "sam");

        FollowRequest request1 = buildFollowRequest(1L, sender1, receiver);
        FollowRequest request2 = buildFollowRequest(2L, sender2, receiver);

        when(followRequestRepository.findByReceiver(receiver))
                .thenReturn(List.of(request1, request2));

        List<FollowRequestDto> result = followRequestService.findByReceiver(receiver);

        assertThat(result).hasSize(2);
        verify(followRequestRepository).findByReceiver(receiver);
    }

    @Test
    void findByReceiver_shouldReturnEmpty_whenNoRequests() {
        User receiver = buildUser(1L, "chris");

        when(followRequestRepository.findByReceiver(receiver))
                .thenReturn(List.of());

        List<FollowRequestDto> result = followRequestService.findByReceiver(receiver);

        assertThat(result).isEmpty();
        verify(followRequestRepository).findByReceiver(receiver);
    }

    // ---------- sendFollowRequest ----------

    @Test
    void sendFollowRequest_shouldCreateFollower_whenReceiverIsPublic() {
        User sender = buildUser(1L, "chris");
        User receiver = buildUser(2L, "alex");
        receiver.setIsPublic(true);

        when(followerRepository.existsByFollowerAndFollowed(sender, receiver))
                .thenReturn(false);
        when(followRequestRepository.existsBySenderAndReceiverAndStatus(
                sender, receiver, FollowRequest.FollowRequestStatus.PENDING))
                .thenReturn(false);

        Follower savedFollower = new Follower();
        savedFollower.setFollower(sender);
        savedFollower.setFollowed(receiver);
        when(followerRepository.save(any(Follower.class))).thenReturn(savedFollower);

        followRequestService.sendFollowRequest(sender, receiver);

        verify(followerRepository).save(any(Follower.class));
        verify(notificationService).notifyFollow(eq(receiver), any(Follower.class));
        verify(followRequestRepository, never()).save(any(FollowRequest.class));
    }

    @Test
    void sendFollowRequest_shouldCreateRequest_whenReceiverIsPrivate() {
        User sender = buildUser(1L, "chris");
        User receiver = buildUser(2L, "alex");
        receiver.setIsPublic(false);

        when(followerRepository.existsByFollowerAndFollowed(sender, receiver))
                .thenReturn(false);
        when(followRequestRepository.existsBySenderAndReceiverAndStatus(
                sender, receiver, FollowRequest.FollowRequestStatus.PENDING))
                .thenReturn(false);

        FollowRequest savedRequest = buildFollowRequest(1L, sender, receiver);
        when(followRequestRepository.save(any(FollowRequest.class))).thenReturn(savedRequest);

        followRequestService.sendFollowRequest(sender, receiver);

        verify(followRequestRepository).save(any(FollowRequest.class));
        verify(notificationService).notifyFollowRequest(eq(receiver), any(FollowRequest.class));
        verify(followerRepository, never()).save(any(Follower.class));
    }

    @Test
    void sendFollowRequest_shouldThrow_whenTryingToFollowSelf() {
        User user = buildUser(1L, "chris");

        assertThatThrownBy(() -> followRequestService.sendFollowRequest(user, user))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No puedes seguirte a ti mismo");

        verify(followerRepository, never()).save(any());
        verify(followRequestRepository, never()).save(any());
    }

    @Test
    void sendFollowRequest_shouldThrow_whenAlreadyFollowing() {
        User sender = buildUser(1L, "chris");
        User receiver = buildUser(2L, "alex");

        when(followerRepository.existsByFollowerAndFollowed(sender, receiver))
                .thenReturn(true);

        assertThatThrownBy(() -> followRequestService.sendFollowRequest(sender, receiver))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ya sigues a este usuario");

        verify(followerRepository, never()).save(any());
        verify(followRequestRepository, never()).save(any());
    }

    @Test
    void sendFollowRequest_shouldThrow_whenPendingRequestExists() {
        User sender = buildUser(1L, "chris");
        User receiver = buildUser(2L, "alex");
        receiver.setIsPublic(false);

        when(followerRepository.existsByFollowerAndFollowed(sender, receiver))
                .thenReturn(false);
        when(followRequestRepository.existsBySenderAndReceiverAndStatus(
                sender, receiver, FollowRequest.FollowRequestStatus.PENDING))
                .thenReturn(true);

        assertThatThrownBy(() -> followRequestService.sendFollowRequest(sender, receiver))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ya has enviado una solicitud");

        verify(followerRepository, never()).save(any());
        verify(followRequestRepository, never()).save(any());
    }

    // ---------- acceptRequest ----------

    @Test
    void acceptRequest_shouldCreateFollowerAndDeleteRequest() {
        User sender = buildUser(1L, "chris");
        User receiver = buildUser(2L, "alex");
        FollowRequest request = buildFollowRequest(1L, sender, receiver);

        when(followRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        Notification notification = new Notification();
        notification.setRead(false);
        when(notificationRepository.findByUserAndSenderAndTypeAndReferenceIdAndReadFalse(
                receiver, sender, Notification.NotificationType.FOLLOW_REQUEST, 1L))
                .thenReturn(Optional.of(notification));

        Follower savedFollower = new Follower();
        savedFollower.setFollower(sender);
        savedFollower.setFollowed(receiver);
        when(followerRepository.save(any(Follower.class))).thenReturn(savedFollower);

        FollowRequestActionResponse response = followRequestService.acceptRequest(1L, receiver);

        assertThat(response).isNotNull();
        assertThat(response.getRequestId()).isEqualTo(1L);
        assertThat(response.getSenderId()).isEqualTo(1L);
        assertThat(response.getReceiverId()).isEqualTo(2L);
        assertThat(response.getStatus()).isEqualTo(FollowRequestActionResponse.FollowRequestStatus.ACCEPTED);

        verify(followerRepository).save(any(Follower.class));
        verify(followRequestRepository).delete(request);
        verify(notificationService).notifyFollow(eq(receiver), any(Follower.class));
        verify(notificationService).notifyFollowRequestAccepted(eq(receiver), any(Follower.class));
        assertThat(notification.isRead()).isTrue();
    }

    @Test
    void acceptRequest_shouldThrow_whenRequestNotFound() {
        User receiver = buildUser(2L, "alex");
        
        when(followRequestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> followRequestService.acceptRequest(999L, receiver))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Solicitud no encontrada");

        verify(followerRepository, never()).save(any());
        verify(followRequestRepository, never()).delete(any());
    }

    @Test
    void acceptRequest_shouldThrow_whenUserIsNotReceiver() {
        User sender = buildUser(1L, "chris");
        User receiver = buildUser(2L, "alex");
        User other = buildUser(3L, "sam");
        FollowRequest request = buildFollowRequest(1L, sender, receiver);

        when(followRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> followRequestService.acceptRequest(1L, other))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No puedes aceptar esta solicitud");

        verify(followerRepository, never()).save(any());
        verify(followRequestRepository, never()).delete(any());
    }

    @Test
    void acceptRequest_shouldWork_whenNoNotificationExists() {
        User sender = buildUser(1L, "chris");
        User receiver = buildUser(2L, "alex");
        FollowRequest request = buildFollowRequest(1L, sender, receiver);

        when(followRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(notificationRepository.findByUserAndSenderAndTypeAndReferenceIdAndReadFalse(
                any(), any(), any(), any()))
                .thenReturn(Optional.empty());

        Follower savedFollower = new Follower();
        when(followerRepository.save(any(Follower.class))).thenReturn(savedFollower);

        FollowRequestActionResponse response = followRequestService.acceptRequest(1L, receiver);

        assertThat(response).isNotNull();
        verify(followerRepository).save(any(Follower.class));
        verify(followRequestRepository).delete(request);
    }

    // ---------- rejectRequest ----------

    @Test
    void rejectRequest_shouldDeleteRequest() {
        User sender = buildUser(1L, "chris");
        User receiver = buildUser(2L, "alex");
        FollowRequest request = buildFollowRequest(1L, sender, receiver);

        when(followRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        Notification notification = new Notification();
        notification.setRead(false);
        when(notificationRepository.findByUserAndSenderAndTypeAndReferenceIdAndReadFalse(
                receiver, sender, Notification.NotificationType.FOLLOW_REQUEST, 1L))
                .thenReturn(Optional.of(notification));

        FollowRequestActionResponse response = followRequestService.rejectRequest(1L, receiver);

        assertThat(response).isNotNull();
        assertThat(response.getRequestId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(FollowRequestActionResponse.FollowRequestStatus.REJECTED);

        verify(followRequestRepository).delete(request);
        verify(followerRepository, never()).save(any());
        assertThat(notification.isRead()).isTrue();
    }

    @Test
    void rejectRequest_shouldThrow_whenRequestNotFound() {
        User receiver = buildUser(2L, "alex");
        
        when(followRequestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> followRequestService.rejectRequest(999L, receiver))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Solicitud no encontrada");

        verify(followRequestRepository, never()).delete(any());
    }

    @Test
    void rejectRequest_shouldThrow_whenUserIsNotReceiver() {
        User sender = buildUser(1L, "chris");
        User receiver = buildUser(2L, "alex");
        User other = buildUser(3L, "sam");
        FollowRequest request = buildFollowRequest(1L, sender, receiver);

        when(followRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> followRequestService.rejectRequest(1L, other))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No puedes rechazar esta solicitud");

        verify(followRequestRepository, never()).delete(any());
    }

    @Test
    void rejectRequest_shouldWork_whenNoNotificationExists() {
        User sender = buildUser(1L, "chris");
        User receiver = buildUser(2L, "alex");
        FollowRequest request = buildFollowRequest(1L, sender, receiver);

        when(followRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(notificationRepository.findByUserAndSenderAndTypeAndReferenceIdAndReadFalse(
                any(), any(), any(), any()))
                .thenReturn(Optional.empty());

        FollowRequestActionResponse response = followRequestService.rejectRequest(1L, receiver);

        assertThat(response).isNotNull();
        verify(followRequestRepository).delete(request);
    }

    // ---------- helpers ----------

    private User buildUser(Long id, String username) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setEmail(username + "@example.com");
        u.setIsPublic(false); // Default private
        return u;
    }

    private FollowRequest buildFollowRequest(Long id, User sender, User receiver) {
        FollowRequest fr = new FollowRequest();
        fr.setId(id);
        fr.setSender(sender);
        fr.setReceiver(receiver);
        fr.setStatus(FollowRequest.FollowRequestStatus.PENDING);
        return fr;
    }
}