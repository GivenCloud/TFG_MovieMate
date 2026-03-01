package com.moviemate.scheduler;

import com.moviemate.repository.ContentRepository;
import com.moviemate.service.ContentService;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CacheCleaner {

    private final ContentRepository contentRepository;
    private final ContentService contentService;
    
    // 3AM: 1. Limpia basura + 2. Refresca popular
    @Scheduled(cron = "0 0 3 * * *")
    public void nightlyMaintenance() {
        // 1. Limpia contenido sin uso >90 días
        LocalDateTime threshold = LocalDateTime.now().minusDays(90);
        contentRepository.deleteUnused(threshold);
        
        // 2. Refresca TOP 100 populares
        List<Long> topIds = contentRepository.findTopViewedContentIds(100);
        topIds.forEach(contentService::refreshAsync);
    }
}
