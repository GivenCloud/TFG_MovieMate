package com.moviemate.service;

import com.moviemate.dto.ReportRequest;
import com.moviemate.dto.ReportResponse;
import com.moviemate.dto.UserResponse;
import com.moviemate.entity.ContentReport;
import com.moviemate.entity.User;
import com.moviemate.repository.ContentReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ContentReportServiceTest {

    private ContentReportRepository reportRepository;
    private UserService userService;
    private RatingService ratingService;
    private CommentService commentService;
    private NotificationService notificationService;
    private ContentReportService contentReportService;

    @BeforeEach
    void setUp() {
        reportRepository = mock(ContentReportRepository.class);
        userService = mock(UserService.class);
        ratingService = mock(RatingService.class);
        commentService = mock(CommentService.class);
        notificationService = mock(NotificationService.class);
        contentReportService = new ContentReportService(
                reportRepository,
                userService,
                ratingService,
                commentService,
                notificationService
        );

        when(userService.mapToUserResponse(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return UserResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .avatarUrl(user.getAvatarUrl())
                    .build();
        });
    }

    @Test
    void createReport_shouldSaveAndMapResponse() {
        User reporter = buildUser(1L, "moderator");

        ReportRequest request = new ReportRequest();
        request.setTargetType(ContentReport.TargetType.RATING);
        request.setTargetId(10L);
        request.setReason(ContentReport.ReportReason.SPAM);

        when(reportRepository.save(any(ContentReport.class))).thenAnswer(invocation -> {
            ContentReport report = invocation.getArgument(0);
            report.setId(99L);
            report.setCreatedAt(LocalDateTime.now());
            return report;
        });

        ReportResponse response = contentReportService.createReport(reporter, request);

        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getTargetType()).isEqualTo(ContentReport.TargetType.RATING);
        assertThat(response.getReporter().getUsername()).isEqualTo("moderator");
        verify(reportRepository).save(any(ContentReport.class));
    }

    @Test
    void getReports_shouldUseStatusQuery_whenStatusIsProvided() {
        ContentReport report = buildReport(1L, ContentReport.ReportStatus.PENDING);
        when(reportRepository.findByStatusOrderByCreatedAtDesc(ContentReport.ReportStatus.PENDING))
                .thenReturn(List.of(report));

        List<ReportResponse> responses = contentReportService.getReports(ContentReport.ReportStatus.PENDING);

        assertThat(responses).hasSize(1);
        verify(reportRepository).findByStatusOrderByCreatedAtDesc(ContentReport.ReportStatus.PENDING);
        verify(reportRepository, never()).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void getReports_shouldUseAllQuery_whenStatusIsNull() {
        ContentReport report = buildReport(1L, ContentReport.ReportStatus.PENDING);
        when(reportRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(report));

        List<ReportResponse> responses = contentReportService.getReports(null);

        assertThat(responses).hasSize(1);
        verify(reportRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void resolveReport_shouldResolveRatingReportAndNotifyAuthor() {
        ContentReport report = buildReport(1L, ContentReport.ReportStatus.PENDING);
        report.setTargetType(ContentReport.TargetType.RATING);
        report.setTargetId(10L);
        report.setReason(ContentReport.ReportReason.SPAM);

        User author = buildUser(2L, "rating-owner");

        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(reportRepository.save(any(ContentReport.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ratingService.getRatingAuthor(10L)).thenReturn(author);

        ReportResponse response = contentReportService.resolveReport(1L);

        assertThat(response.getStatus()).isEqualTo(ContentReport.ReportStatus.RESOLVED);
        verify(ratingService).getRatingAuthor(10L);
        verify(ratingService).adminDeleteRating(10L);
        verify(notificationService).notifyContentRemoved(author, report);
    }

    @Test
    void resolveReport_shouldResolveCommentReport() {
        ContentReport report = buildReport(1L, ContentReport.ReportStatus.PENDING);
        report.setTargetType(ContentReport.TargetType.COMMENT);
        report.setTargetId(20L);
        report.setReason(ContentReport.ReportReason.INAPPROPRIATE);

        User author = buildUser(3L, "comment-owner");

        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(reportRepository.save(any(ContentReport.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(commentService.getCommentAuthor(20L)).thenReturn(author);

        contentReportService.resolveReport(1L);

        verify(commentService).getCommentAuthor(20L);
        verify(commentService).adminDelete(20L);
        verify(notificationService).notifyContentRemoved(author, report);
    }

    @Test
    void dismissReport_shouldChangeStatus() {
        ContentReport report = buildReport(1L, ContentReport.ReportStatus.PENDING);
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(reportRepository.save(any(ContentReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReportResponse response = contentReportService.dismissReport(1L);

        assertThat(response.getStatus()).isEqualTo(ContentReport.ReportStatus.DISMISSED);
        verify(reportRepository).save(report);
    }

    @Test
    void resolveReport_shouldThrow_whenReportNotFound() {
        when(reportRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contentReportService.resolveReport(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Reporte no encontrado");
    }

    private ContentReport buildReport(Long id, ContentReport.ReportStatus status) {
        ContentReport report = new ContentReport();
        report.setId(id);
        report.setReporter(buildUser(1L, "moderator"));
        report.setTargetType(ContentReport.TargetType.RATING);
        report.setTargetId(10L);
        report.setReason(ContentReport.ReportReason.SPAM);
        report.setStatus(status);
        report.setCreatedAt(LocalDateTime.now());
        return report;
    }

    private User buildUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setAvatarUrl("avatar.png");
        return user;
    }
}