package com.moviemate.scheduler;

import com.moviemate.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CacheCleaner {

    private final ContentRepository contentRepository;

    // Ejecuta cada día a las 3 AM
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanUnusedContent() {

        LocalDateTime threshold = LocalDateTime.now().minusDays(90);

        contentRepository.deleteUnused(threshold);
    }
}
