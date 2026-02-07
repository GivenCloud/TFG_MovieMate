package com.moviemate.security;

import com.moviemate.entity.User;
import com.moviemate.exception.ProfilePrivateException;
import com.moviemate.repository.FollowerRepository;
import com.moviemate.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("profileSecurity")
public class ProfileSecurityService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FollowerRepository followerRepository;
    
    public boolean canViewProfile(String currentUsername, Long targetUserId) {
        User currentUser = userRepository.findByUsername(currentUsername).orElse(null);
        User targetUser = userRepository.findById(targetUserId).orElse(null);
        
        if (targetUser == null) {
            throw new EntityNotFoundException("Usuario no encontrado");
        }
        
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        
        // 1. Propio perfil: OK
        if (currentUserId != null && currentUserId.equals(targetUserId)) {
            return true;
        }
        
        // 2. Público: OK
        if (targetUser.getIsPublic()) {
            return true;
        }
        
        // 3. ¿Soy seguidor del target?
        if (currentUserId != null) {
            boolean isFollower = followerRepository.existsByFollowerAndFollowed(currentUser, targetUser);
            
            if (isFollower) {
                return true;
            }
        }
        
        // 4. Privado + no propio + no seguidor → BLOQUEADO
        throw new ProfilePrivateException("Este perfil es privado y no puedes consultarlo");
    }
}

