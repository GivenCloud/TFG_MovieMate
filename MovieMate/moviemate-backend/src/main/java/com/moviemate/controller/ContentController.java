package com.moviemate.controller;

import com.moviemate.entity.Content;
import com.moviemate.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentController {
    
    private final ContentRepository contentRepository;
    
    @GetMapping
    public ResponseEntity<List<Content>> getAllContent() {
        return ResponseEntity.ok(contentRepository.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Content> getContentById(@PathVariable Long id) {
        ResponseEntity<Content> content = contentRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
        
        if (content.getStatusCode().is2xxSuccessful()) {
            return content;
        } else {
            return ResponseEntity.notFound()
                .header("X-Message", "Contenido no encontrado con ID: " + id)
                .build();
        }
    }
}