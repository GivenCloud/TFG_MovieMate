package com.moviemate.controller;

import com.moviemate.dto.ActivityResponse;
import com.moviemate.entity.User;
import com.moviemate.security.CustomUserDetails;
import com.moviemate.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @Operation(
            summary = "Obtiene el feed personal del usuario autenticado",
            description = "Devuelve actividades relacionadas con los usuarios que sigues o tu propia actividad.",
            parameters = {
                    @Parameter(
                            name = "page",
                            description = "Número de página",
                            example = "0"
                    ),
                    @Parameter(
                            name = "size",
                            description = "Cantidad de elementos por página",
                            example = "20"
                    )
            }
    )
    @GetMapping("/personal")
    public ResponseEntity<Page<ActivityResponse>> getPersonalFeed(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        User user = userDetails.getUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<ActivityResponse> feed = activityService.getUserFeed(user, pageable);

        return ResponseEntity.ok(feed);
    }

    @Operation(
            summary = "Obtiene el feed global",
            description = "Devuelve actividades públicas de todos los usuarios de la plataforma.",
            parameters = {
                    @Parameter(
                            name = "page",
                            description = "Número de página",
                            example = "0"
                    ),
                    @Parameter(
                            name = "size",
                            description = "Cantidad de elementos por página",
                            example = "20"
                    )
            }
    )
    @GetMapping("/global")
    public ResponseEntity<Page<ActivityResponse>> getGlobalFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ActivityResponse> feed = activityService.getGlobalActivity(pageable);

        return ResponseEntity.ok(feed);
    }
}
