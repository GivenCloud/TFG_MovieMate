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
        com.moviemate.entity.Content content = contentService.getOrSyncByTmdb(
                request.getTmdbId().intValue(), 
                request.getContentType()
        );
        return ResponseEntity.ok(listService.addContentToList(user, listId, content.getId()));
    }

    @Operation(
            summary = "Eliminar contenido de una lista",
            description = "Elimina un elemento asociado a una lista."
    )
    @DeleteMapping("/{listId}/content/{contentId}")
    public ResponseEntity<Void> removeContentFromList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long listId,
            @PathVariable Long contentId) {
        User user = userDetails.getUser();
        listService.removeContentFromList(user, listId, contentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Obtener las listas del usuario autenticado",
            description = "Devuelve todas las listas creadas por el usuario."
    )
    @GetMapping("/my-lists")
    public ResponseEntity<List<ListResponse>> getUserLists(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        return ResponseEntity.ok(listService.getUserLists(user));
    }

    @Operation(
            summary = "Obtener todas las listas públicas",
            description = "Devuelve todas las listas visibles para cualquier usuario."
    )
    @GetMapping("/public")
    public ResponseEntity<List<ListResponse>> getPublicLists() {
        return ResponseEntity.ok(listService.getPublicLists());
    }
}
