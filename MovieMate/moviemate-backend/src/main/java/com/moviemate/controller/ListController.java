package com.moviemate.controller;

import com.moviemate.dto.AddToListRequest;
import com.moviemate.dto.ListRequest;
import com.moviemate.dto.ListResponse;
import com.moviemate.entity.User;
import com.moviemate.security.CustomUserDetails;
import com.moviemate.service.ContentService;
import com.moviemate.service.ListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/api/lists")
@RequiredArgsConstructor
public class ListController {
    
    private final ListService listService;
    private final ContentService contentService;

    @Operation(
            summary = "Crear una lista",
            description = "Permite crear una lista personalizada para un usuario.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "Lista Pública",
                                            value = """
                                            {
                                              "name": "Favoritas",
                                              "description": "Mis películas favoritas",
                                              "isPublic": true,
                                              "listType": "CUSTOM"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Lista Privada",
                                            value = """
                                            {
                                              "name": "Pendientes de ver",
                                              "description": "Contenido que quiero ver en el futuro",
                                              "isPublic": false,
                                              "listType": "WATCHLIST"
                                            }
                                            """
                                    )
                            }
                    )
            )
    )
    @PostMapping
    public ResponseEntity<ListResponse> createList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @org.springframework.web.bind.annotation.RequestBody ListRequest request) {
        User user = userDetails.getUser();
        return ResponseEntity.ok(listService.createList(user, request));
    }

    @Operation(
            summary = "Añadir contenido a una lista",
            description = "Agrega un elemento (película/serie) a una lista existente."
    )
    @PostMapping("/{listId}/content")
    public ResponseEntity<ListResponse> addContentToList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long listId,
            @org.springframework.web.bind.annotation.RequestBody AddToListRequest request) {
        User user = userDetails.getUser();
        com.moviemate.entity.Content content = contentService.getOrFetch(
                request.getTmdbId().intValue()
        );
        return ResponseEntity.ok(listService.addContentToList(user, listId, content.getTmdbId()));
    }

    @Operation(
            summary = "Eliminar contenido de una lista",
            description = "Elimina un elemento asociado a una lista."
    )
    @DeleteMapping("/{listId}/content/{tmdbId}")
    public ResponseEntity<Void> removeContentFromList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long listId,
            @PathVariable Integer tmdbId) {
        User user = userDetails.getUser();
        listService.removeContentFromList(user, listId, tmdbId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Eliminar una lista",
            description = "Elimina una lista personalizada del usuario autenticado."
    )
    @DeleteMapping("/{listId}")
    public ResponseEntity<Void> deleteList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long listId) {
        User user = userDetails.getUser();
        listService.deleteList(user, listId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Actualizar una lista",
            description = "Actualiza el nombre, descripción o visibilidad de una lista personalizada."
    )
    @PutMapping("/{listId}")
    public ResponseEntity<ListResponse> updateList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long listId,
            @Valid @org.springframework.web.bind.annotation.RequestBody ListRequest request) {
        User user = userDetails.getUser();
        return ResponseEntity.ok(listService.updateList(user, listId, request));
    }

    @Operation(
            summary = "Obtener una lista por ID",
            description = "Devuelve una lista pública a cualquier usuario, o una lista privada solo a su propietario."
    )
    @GetMapping("/{listId}")
    public ResponseEntity<ListResponse> getListById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long listId) {
        Long currentUserId = userDetails != null ? userDetails.getUser().getId() : null;
        return ResponseEntity.ok(listService.getListById(listId, currentUserId));
    }

    @Operation(
            summary = "Obtener todas las listas públicas",
            description = "Devuelve todas las listas visibles para cualquier usuario."
    )
    @GetMapping("/public")
    public ResponseEntity<List<ListResponse>> getPublicLists(
                Authentication authentication) {
        Long userId = null;
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            userId = userDetails.getUser().getId();
        }
        return ResponseEntity.ok(listService.getPublicLists(userId));
    }
}
