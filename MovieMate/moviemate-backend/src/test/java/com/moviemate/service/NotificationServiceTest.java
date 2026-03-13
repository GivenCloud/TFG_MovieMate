package com.moviemate.service;

import com.moviemate.dto.NotificationDto;
import com.moviemate.entity.FollowRequest;
import com.moviemate.entity.Follower;
import com.moviemate.entity.Notification;
import com.moviemate.entity.ReviewLike;
import com.moviemate.entity.Rating;
import com.moviemate.entity.User;
import com.moviemate.repository.FollowRequestRepository;
import com.moviemate.repository.FollowerRepository;
import com.moviemate.repository.NotificationRepository;
import com.moviemate.repository.ReviewLikeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    private NotificationRepository notificationRepository;
    private FollowRequestRepository followRequestRepository;
    private FollowerRepository followerRepository;
    private ReviewLikeRepository reviewLikeRepository;
    private SimpMessagingTemplate messagingTemplate;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationRepository = mock(NotificationRepository.class);
        followRequestRepository = mock(FollowRequestRepository.class);
        followerRepository = mock(FollowerRepository.class);
        reviewLikeRepository = mock(ReviewLikeRepository.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        
        notificationService = new NotificationService(
            notificationRepository,
            messagingTemplate
        );
    }

    // ---------- notifyFollowRequest ----------

    @Test
    void notifyFollowRequest_shouldCreateNotification_whenNotExists() {
        User sender = buildUser(1L, "chris");
        User receiver = buildUser(2L, "alex");
        FollowRequest request = buildFollowRequest(10L, sender, receiver);

        when(notificationRepository.findByUserAndSenderAndTypeAndReferenceIdAndReadFalse(
                receiver, sender, Notification.NotificationType.FOLLOW_REQUEST, 10L))
                .thenReturn(Optional.empty());

        Notification savedNotification = new Notification();
        savedNotification.setId(1L);
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        notificationService.notifyFollowRequest(receiver, request);

        verify(notificationRepository).save(any(Notification.class));
        verify(messagingTemplate).convertAndSendToUser(
                eq("alex"),
                eq("/queue/notifications"),
                any(NotificationDto.class)
        );
    }

    @Test
    void notifyFollowRequest_shouldNotCreateDuplicate_whenAlreadyExists() {
        User sender = buildUser(1L, "chris");
        User receiver = buildUser(2L, "alex");
        FollowRequest request = buildFollowRequest(10L, sender, receiver);

        Notification existingNotification = new Notification();
        when(notificationRepository.findByUserAndSenderAndTypeAndReferenceIdAndReadFalse(
                receiver, sender, Notification.NotificationType.FOLLOW_REQUEST, 10L))
                .thenReturn(Optional.of(existingNotification));

        notificationService.notifyFollowRequest(receiver, request);

        verify(notificationRepository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSendToUser(any(), any(), any());
    }

    // ---------- notifyFollow ----------

    @Test
    void notifyFollow_shouldCreateNotification_whenNotExists() {
        User sender = buildUser(1L, "chris");
        User receiver = buildUser(2L, "alex");
        Follower follower = buildFollower(10L, sender, receiver);

        when(notificationRepository.findByUserAndSenderAndTypeAndReferenceIdAndReadFalse(
                receiver, sender, Notification.NotificationType.FOLLOWER, 10L))
                .thenReturn(Optional.empty());

        Notification savedNotification = new Notification();
        savedNotification.setId(1L);
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        notificationService.notifyFollow(receiver, follower);

        verify(notificationRepository).save(any(Notification.class));
        verify(messagingTemplate).convertAndSendToUser(
                eq("alex"),
                eq("/queue/notifications"),
                any(NotificationDto.class)
        );
    }

    @Test
    void notifyFollow_shouldNotCreateDuplicate_whenAlreadyExists() {
        User sender = buildUser(1L, "chris");
        User receiver = buildUser(2L, "alex");
        Follower follower = buildFollower(10L, sender, receiver);

        Notification existingNotification = new Notification();
        when(notificationRepository.findByUserAndSenderAndTypeAndReferenceIdAndReadFalse(
                receiver, sender, Notification.NotificationType.FOLLOWER, 10L))
                .thenReturn(Optional.of(existingNotification));

        notificationService.notifyFollow(receiver, follower);

        verify(notificationRepository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSendToUser(any(), any(), any());
    }

    // ---------- notifyFollowRequestAccepted ----------

    @Test
    void notifyFollowRequestAccepted_shouldCreateNotification_whenNotExists() {
        User sender = buildUser(1L, "chris");
        User receiver = buildUser(2L, "alex");
        Follower follower = buildFollower(10L, sender, receiver);

        when(notificationRepository.findByUserAndSenderAndTypeAndReferenceIdAndReadFalse(
                receiver, sender, Notification.NotificationType.FOLLOW_REQUEST_ACCEPTED, 10L))
                .thenReturn(Optional.empty());

        Notification savedNotification = new Notification();
        savedNotification.setId(1L);
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        notificationService.notifyFollowRequestAccepted(receiver, follower);

        verify(notificationRepository).save(any(Notification.class));
        verify(messagingTemplate).convertAndSendToUser(
                eq("alex"),
                eq("/queue/notifications"),
                any(NotificationDto.class)
        );
    }

    @Test
    void notifyFollowRequestAccepted_shouldNotCreateDuplicate_whenAlreadyExists() {
        User sender = buildUser(1L, "chris");
        User receiver = buildUser(2L, "alex");
        Follower follower = buildFollower(10L, sender, receiver);

        Notification existingNotification = new Notification();
        when(notificationRepository.findByUserAndSenderAndTypeAndReferenceIdAndReadFalse(
                receiver, sender, Notification.NotificationType.FOLLOW_REQUEST_ACCEPTED, 10L))
                .thenReturn(Optional.of(existingNotification));

        notificationService.notifyFollowRequestAccepted(receiver, follower);

        verify(notificationRepository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSendToUser(any(), any(), any());
    }

    // ---------- sendLikeNotification ----------

    @Test
    void sendLikeNotification_shouldCreateNotification_whenNotExists() {
        User sender = buildUser(1L, "chris");
        User receiver = buildUser(2L, "alex");
        Rating rating = buildRating(10L, receiver);
        ReviewLike reviewLike = buildReviewLike(5L, sender, rating);

        when(notificationRepository.findByUserAndSenderAndTypeAndReferenceIdAndReadFalse(
                receiver, sender, Notification.NotificationType.REVIEW_LIKE, 10L))
                .thenReturn(Optional.empty());

        Notification savedNotification = new Notification();
        savedNotification.setId(1L);
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        notificationService.sendLikeNotification(receiver, reviewLike);

        verify(notificationRepository).save(any(Notification.class));
        verify(messagingTemplate).convertAndSendToUser(
                eq("alex"),
                eq("/queue/notifications"),
                any(NotificationDto.class)
        );
    }

    @Test
    void sendLikeNotification_shouldNotCreateDuplicate_whenAlreadyExists() {
        User sender = buildUser(1L, "chris");
        User receiver = buildUser(2L, "alex");
        Rating rating = buildRating(10L, receiver);
        ReviewLike reviewLike = buildReviewLike(5L, sender, rating);

        Notification existingNotification = new Notification();
        when(notificationRepository.findByUserAndSenderAndTypeAndReferenceIdAndReadFalse(
                receiver, sender, Notification.NotificationType.REVIEW_LIKE, 10L))
                .thenReturn(Optional.of(existingNotification));

        notificationService.sendLikeNotification(receiver, reviewLike);

        verify(notificationRepository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSendToUser(any(), any(), any());
    }

    // ---------- getNotifications ----------

    @Test
    void getNotifications_shouldReturnAllNotifications() {
        User user = buildUser(1L, "chris");
        
        Notification notif1 = buildNotification(1L, user, Notification.NotificationType.FOLLOWER);
        Notification notif2 = buildNotification(2L, user, Notification.NotificationType.REVIEW_LIKE);

        when(notificationRepository.findByUserOrderByCreatedAtDesc(user))
                .thenReturn(List.of(notif1, notif2));

        when(followerRepository.findById(any())).thenReturn(Optional.empty());
        when(reviewLikeRepository.findById(any())).thenReturn(Optional.empty());

        List<NotificationDto> notifications = notificationService.getNotifications(user);

        assertThat(notifications).hasSize(2);
        verify(notificationRepository).findByUserOrderByCreatedAtDesc(user);
    }

    @Test
    void getNotifications_shouldReturnEmpty_whenNoNotifications() {
        User user = buildUser(1L, "chris");

        when(notificationRepository.findByUserOrderByCreatedAtDesc(user))
                .thenReturn(List.of());

        List<NotificationDto> notifications = notificationService.getNotifications(user);

        assertThat(notifications).isEmpty();
    }

    // ---------- markAsRead ----------

    @Test
    void markAsRead_shouldMarkNotification_whenUserIsOwner() {
        User user = buildUser(1L, "chris");
        Notification notification = buildNotification(1L, user, Notification.NotificationType.FOLLOWER);
        notification.setRead(false);

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(1L, user);

        assertThat(notification.isRead()).isTrue();
        verify(notificationRepository).findById(1L);
    }

    @Test
    void markAsRead_shouldThrow_whenNotificationNotFound() {
        User user = buildUser(1L, "chris");

        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(999L, user))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Notificación no encontrada");
    }

    @Test
    void markAsRead_shouldThrow_whenUserIsNotOwner() {
        User owner = buildUser(1L, "chris");
        User other = buildUser(2L, "alex");
        Notification notification = buildNotification(1L, owner, Notification.NotificationType.FOLLOWER);

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.markAsRead(1L, other))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No puedes modificar esta notificación");

        assertThat(notification.isRead()).isFalse();
    }

    // ---------- markAllAsRead ----------

    @Test
    void markAllAsRead_shouldCallRepository() {
        User user = buildUser(1L, "chris");

        notificationService.markAllAsRead(user);

        verify(notificationRepository).markAllAsReadByUser(user);
    }

    // ---------- helpers ----------

    private User buildUser(Long id, String username) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setEmail(username + "@example.com");
        return u;
    }

    private FollowRequest buildFollowRequest(Long id, User sender, User receiver) {
        FollowRequest fr = new FollowRequest();
        fr.setId(id);
        fr.setSender(sender);
        fr.setReceiver(receiver);
        return fr;
    }

    private Follower buildFollower(Long id, User follower, User followed) {
        Follower f = new Follower();
        f.setId(id);
        f.setFollower(follower);
        f.setFollowed(followed);
        return f;
    }

    private Rating buildRating(Long id, User user) {
        Rating r = new Rating();
        r.setId(id);
        r.setUser(user);
        r.setRating(5);
        return r;
    }

    private ReviewLike buildReviewLike(Long id, User user, Rating rating) {
        ReviewLike rl = new ReviewLike();
        rl.setId(id);
        rl.setUser(user);
        rl.setRating(rating);
        return rl;
    }

    private Notification buildNotification(Long id, User user, Notification.NotificationType type) {
        Notification n = new Notification();
        n.setId(id);
        n.setUser(user);
        n.setType(type);
        n.setReferenceId(10L);
        n.setRead(false);
        return n;
    }
}