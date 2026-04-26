package com.moviemate.scheduler;

import com.moviemate.repository.ContentRepository;
import com.moviemate.service.ContentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CacheCleanerTest {

    private ContentRepository contentRepository;
    private ContentService contentService;
    private CacheCleaner cacheCleaner;

    @BeforeEach
    void setUp() {
        contentRepository = mock(ContentRepository.class);
        contentService = mock(ContentService.class);
        cacheCleaner = new CacheCleaner(contentRepository, contentService);
    }

    @Test
    void nightlyMaintenance_shouldDeleteUnusedAndRefreshTopViewed() {
        when(contentRepository.findTopViewedContentIds(100)).thenReturn(List.of(10L, 20L, 30L));

        LocalDateTime before = LocalDateTime.now().minusDays(90).minusSeconds(2);
        cacheCleaner.nightlyMaintenance();
        LocalDateTime after = LocalDateTime.now().minusDays(90).plusSeconds(2);

        ArgumentCaptor<LocalDateTime> thresholdCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(contentRepository).deleteUnused(thresholdCaptor.capture());
        verify(contentRepository).findTopViewedContentIds(100);
        verify(contentService).refreshAsync(10L);
        verify(contentService).refreshAsync(20L);
        verify(contentService).refreshAsync(30L);

        LocalDateTime usedThreshold = thresholdCaptor.getValue();
        assertThat(usedThreshold).isAfterOrEqualTo(before);
        assertThat(usedThreshold).isBeforeOrEqualTo(after);
    }
}
