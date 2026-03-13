package com.moviemate.service;

import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.moviemate.dto.NotificationDto;
import com.moviemate.entity.Comment;
import com.moviemate.entity.ContentReport;
import com.moviemate.entity.FollowRequest;
import com.moviemate.entity.Follower;
import com.moviemate.entity.Notification;
import com.moviemate.entity.Notification.NotificationType;
import com.moviemate.entity.ReviewLike;
import com.moviemate.entity.User;
import com.moviemate.repository.NotificationRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void notifyFollowRequest(User receiver, FollowRequest request) {

        User sender = request.getSender();
        Long requestId = request.getId();

        boolean alreadyExists =
            notificationRepository
                .findByUserAndSenderAndTypeAndReferenceIdAndReadFalse(
                    receiver,
                    sender,
                    NotificationType.FOLLOW_REQUEST,
                    requestId
                )
                .isPresent();

        if (alreadyExists) {
            return; // NO SPAM
        }

        Notification notification = new Notification();
        notification.setUser(receiver);
        notification.setSender(sender);
        notification.setType(NotificationType.FOLLOW_REQUEST);
        notification.setReferenceId(request.getId());

        notificationRepository.save(notification);

        NotificationDto dto = toDto(notification);

        // tiempo real
        messagingTemplate.convertAndSendToUser(
            receiver.getUsername(),
            "/queue/notifications",
            dto
        );
    }

    @Transactional
    public void notifyFollow(User receiver, Follower follower) {

        User sender = follower.getFollower();
        Long followerId = follower.getId();

        boolean alreadyExists =
            notificationRepository
                .findByUserAndSenderAndTypeAndReferenceIdAndReadFalse(
                    receiver,
                    sender,
                    NotificationType.FOLLOWER,
                    followerId
                )
                .isPresent();

        if (alreadyExists) {
            return; // NO SPAM
        }

        Notification notification = new Notification();
        notification.setUser(receiver);
        notification.setType(NotificationType.FOLLOWER);
        notification.setReferenceId(follower.getId());
        notification.setSender(sender);

        notificationRepository.save(notification);

        NotificationDto dto = toDto(notification);

        // tiempo real
        messagingTemplate.convertAndSendToUser(
            receiver.getUsername(),
            "/queue/notifications",
            dto
        );
    }

    @Transactional
    public void notifyFollowRequestAccepted(User receiver, Follower follower) {
        User sender = follower.getFollower();
        Long followerId = follower.getId();

        boolean alreadyExists =
            notificationRepository
                .findByUserAndSenderAndTypeAndReferenceIdAndReadFalse(
                    receiver,
                    sender,
                    NotificationType.FOLLOW_REQUEST_ACCEPTED,
                    followerId
                )
                .isPresent();

        if (alreadyExists) {
            return; // NO SPAM
        }

        Notification notification = new Notification();
        notification.setUser(receiver);
        notification.setType(NotificationType.FOLLOW_REQUEST_ACCEPTED);
        notification.setReferenceId(follower.getId());
        notification.setSender(sender);

        notificationRepository.save(notification);

        NotificationDto dto = toDto(notification);

        // tiempo real
        messagingTemplate.convertAndSendToUser(
            receiver.getUsername(),
            "/queue/notifications",
            dto
        );
    }

    @Transactional
    public void sendLikeNotification(User receiver, ReviewLike reviewLike) {
        User sender = reviewLike.getUser();
        Long ratingId = reviewLike.getRating().getId();

        boolean alreadyExists =
            notificationRepository
                .findByUserAndSenderAndTypeAndReferenceIdAndReadFalse(
                    receiver,
                    sender,
                    NotificationType.REVIEW_LIKE,
                    ratingId
                )
                .isPresent();

        if (alreadyExists) {
            return; // NO SPAM
        }

        Notification notification = new Notification();
        notification.setUser(receiver);
        notification.setSender(sender);
        notification.setType(NotificationType.REVIEW_LIKE);
        notification.setReferenceId(ratingId);

        notificationRepository.save(notification);

        NotificationDto dto = toDto(notification);

        // tiempo real
        messagingTemplate.convertAndSendToUser(
            receiver.getUsername(),
            "/queue/notifications",
            dto
        );
    }

    @Transactional
    public void notifyContentRemoved(User receiver, ContentReport report) {
        String targetLabel = report.getTargetType() == ContentReport.TargetType.RATING
                ? "valoración" : "comentario";
        String reasonLabel = switch (report.getReason()) {
            case SPAM         -> "spam";
            case INAPPROPRIATE -> "contenido inapropiado";
            case SPOILER      -> "spoiler sin marcar";
            case OTHER        -> "incumplimiento de las normas";
        };

        Notification notification = new Notification();
        notification.setUser(receiver);
        notification.setSender(null);
        notification.setType(NotificationType.CONTENT_REMOVED);
        notification.setReferenceId(report.getId());
        notification.setMessage(
            "Tu " + targetLabel + " ha sido eliminado/a por un administrador. Motivo: " + reasonLabel + "."
        );

        notificationRepository.save(notification);

        NotificationDto dto = toDto(notification);
        messagingTemplate.convertAndSendToUser(
            receiver.getUsername(),
            "/queue/notifications",
            dto
        );
    }

    @Transactional
    public void notifyComment(User receiver, Comment comment) {
        User sender = comment.getAuthor();

        Notification notification = new Notification();
        notification.setUser(receiver);
        notification.setSender(sender);
        notification.setType(NotificationType.COMMENT_ON_RATING);
        notification.setReferenceId(comment.getId());

        notificationRepository.save(notification);

        NotificationDto dto = toDto(notification);

        messagingTemplate.convertAndSendToUser(
            receiver.getUsername(),
            "/queue/notifications",
            dto
        );
    }

    @Transactional
    public List<NotificationDto> getNotifications(User user) {

        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
            .stream()
            .map(this::toDto)
            .toList();
    }

    @Transactional
    public void markAsRead(Long notificationId, User user) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("No puedes modificar esta notificación");
        }

        notification.setRead(true);
    }

    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsReadByUser(user);
    }



    private NotificationDto toDto(Notification notification) {

        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setType(notification.getType());
        dto.setReferenceId(notification.getReferenceId());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setMessage(notification.getMessage());

        // El sender se guarda directamente en la notificación al crearla
        if (notification.getSender() != null) {
            populateUserInfo(dto, notification.getSender());
        }

        return dto;
    }

    private void populateUserInfo(NotificationDto dto, User user) {
        dto.setSenderId(user.getId());
        dto.setSenderUsername(user.getUsername());
        dto.setSenderAvatarUrl(user.getAvatarUrl());
    }

    public int getUnreadCount(User user) {
        return notificationRepository.countByUserAndReadFalse(user);
    }
}

