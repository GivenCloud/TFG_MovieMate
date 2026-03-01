package com.moviemate.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.moviemate.entity.Notification;
import com.moviemate.entity.Notification.NotificationType;
import com.moviemate.entity.User;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    @Modifying
    @Query("update Notification n set n.read = true where n.user = :user and n.read = false")
    void markAllAsReadByUser(@Param("user") User user);

    Optional<Notification> findByUserAndSenderAndTypeAndReferenceIdAndReadFalse(
        User user,
        User sender,
        NotificationType type,
        Long referenceId
    );

    int countByUserAndReadFalse(User user);
}