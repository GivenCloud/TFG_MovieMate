package com.moviemate.service;

import com.moviemate.dto.ListRequest;
import com.moviemate.dto.ListResponse;
import com.moviemate.dto.UserResponse;
import com.moviemate.dto.ContentSimpleResponse;
import com.moviemate.entity.List;
import com.moviemate.entity.ListContent;
import com.moviemate.entity.User;
import com.moviemate.entity.Content;
import com.moviemate.repository.ListRepository;
import com.moviemate.repository.ListContentRepository;
import com.moviemate.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListService {
    
    private final ListRepository listRepository;
    private final ListContentRepository listContentRepository;
    private final ContentRepository contentRepository;
    
    @Transactional
    public ListResponse createList(User user, ListRequest request) {
        // Verificar si ya existe una lista del mismo tipo para el usuario
        if (request.getListType() != List.ListType.CUSTOM) {
            listRepository.findByUserAndListType(user, request.getListType())
                .ifPresent(existingList -> {
                    throw new RuntimeException("Ya existe una lista de tipo " + request.getListType() + " para este usuario");
                });
        }
        
        List list = new List();
        list.setUser(user);
        list.setName(request.getName());
        list.setDescription(request.getDescription());
        list.setIsPublic(request.getIsPublic());
        list.setListType(request.getListType());
        
        List savedList = listRepository.save(list);
        return mapToListResponse(savedList);
    }
    
    @Transactional
    public ListResponse addContentToList(User user, Long listId, Long contentId) {
        List list = listRepository.findById(listId)
            .orElseThrow(() -> new RuntimeException("Lista no encontrada"));
            
        // Verificar que el usuario es el propietario de la lista
        if (!list.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("No tienes permisos para modificar esta lista");
        }
        
        Content content = contentRepository.findById(contentId)
            .orElseThrow(() -> new RuntimeException("Contenido no encontrado"));
            
        // Verificar si el contenido ya está en la lista
        if (listContentRepository.existsByListAndContent(list, content)) {
            throw new RuntimeException("El contenido ya está en la lista");
        }
        
        ListContent listContent = new ListContent();
        listContent.setList(list);
        listContent.setContent(content);
        listContentRepository.save(listContent);
        
        // Refrescar la lista para obtener el conteo actualizado
        list = listRepository.findById(listId).orElseThrow();
        return mapToListResponse(list);
    }
    
    @Transactional
    public void removeContentFromList(User user, Long listId, Long contentId) {
        List list = listRepository.findById(listId)
            .orElseThrow(() -> new RuntimeException("Lista no encontrada"));
            
        if (!list.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("No tienes permisos para modificar esta lista");
        }
        
        Content content = contentRepository.findById(contentId)
            .orElseThrow(() -> new RuntimeException("Contenido no encontrado"));
            
        ListContent listContent = listContentRepository.findByListAndContent(list, content)
            .orElseThrow(() -> new RuntimeException("El contenido no está en la lista"));
            
        listContentRepository.delete(listContent);
    }
    
    @Transactional(readOnly = true)
    public java.util.List<ListResponse> getUserLists(User user) {
        return listRepository.findByUserWithContents(user).stream()
            .map(this::mapToListResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public java.util.List<ListResponse> getPublicLists() {
        return listRepository.findPublicListsWithContents().stream()
            .map(this::mapToListResponse)
            .collect(Collectors.toList());
    }
    
    public ListResponse mapToListResponse(List list) {
        return ListResponse.builder()
            .id(list.getId())
            .name(list.getName())
            .description(list.getDescription())
            .isPublic(list.getIsPublic())
            .listType(list.getListType())
            .itemCount(list.getContents() != null ? list.getContents().size() : 0)
            .createdAt(list.getCreatedAt())
            .user(UserResponse.builder()
                .id(list.getUser().getId())
                .username(list.getUser().getUsername())
                .avatarUrl(list.getUser().getAvatarUrl())
                .build())
            .contents(list.getContents() != null ? list.getContents().stream()
                .map(listContent -> ContentSimpleResponse.builder()
                    .id(listContent.getContent().getId())
                    .title(listContent.getContent().getTitle())
                    .posterUrl(listContent.getContent().getPosterUrl())
                    .contentType(listContent.getContent().getContentType())
                    .build())
                .collect(Collectors.toList()) : java.util.Collections.emptyList())
            .build();
    }
}