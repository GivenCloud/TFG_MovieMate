package com.moviemate.service;

import com.moviemate.dto.UserResponse;
import com.moviemate.entity.User;
import com.moviemate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    public User findUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
    
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
    
    public List<UserResponse> searchUsers(Long currentUserId, String query) {
        // Buscar por username o email que contenga la query
        return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query)
            .stream()
            // No quiero que aparezca el usuario actual en los resultados de búsqueda
            .filter(user -> !user.getId().equals(currentUserId))
            .map(this::mapToUserResponse)
            .collect(Collectors.toList());
    }
    
    public List<UserResponse> getSuggestedUsers(Long currentUserId) {
        // Devolver usuarios recientemente registrados (excepto el actual)
        return userRepository.findTop10ByOrderByCreatedAtDesc()
            .stream()
            .filter(user -> !user.getId().equals(currentUserId))
            .map(this::mapToUserResponse)
            .collect(Collectors.toList());
    }
    
    public UserResponse updateUserProfile(User user, String bio, String avatarUrl) {
        user.setBio(bio);
        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            user.setAvatarUrl(avatarUrl);
        }
        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    public UserResponse updateUserPublicStatus(User user, boolean isPublic) {
        user.setIsPublic(isPublic);
        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }
    
    public UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .avatarUrl(user.getAvatarUrl())
            .bio(user.getBio())
            .isPublic(user.getIsPublic())
            .role(user.getRole())
            .banned(user.getBanned())
            .createdAt(user.getCreatedAt())
            .build();
    }
}