package com.moviemate.service;

import com.moviemate.dto.UserResponse;
import com.moviemate.entity.User;
import com.moviemate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
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

    public UserResponse uploadAvatar(User user, MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Solo se permiten imágenes (JPG, PNG, WebP)");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("El archivo no puede superar los 5 MB");
        }

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "avatar";
        String ext = originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.'))
                : ".jpg";

        Path avatarDir = Paths.get(uploadDir, "avatars");
        Files.createDirectories(avatarDir);

        String filename = user.getId() + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8) + ext;
        Files.copy(file.getInputStream(), avatarDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);

        user.setAvatarUrl("/uploads/avatars/" + filename);
        return mapToUserResponse(userRepository.save(user));
    }

    public void changePassword(User user, String currentPassword, String newPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
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