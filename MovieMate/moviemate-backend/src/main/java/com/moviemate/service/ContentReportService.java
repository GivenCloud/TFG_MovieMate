package com.moviemate.service;

import com.moviemate.dto.ReportRequest;
import com.moviemate.dto.ReportResponse;
import com.moviemate.entity.ContentReport;
import com.moviemate.entity.User;
import com.moviemate.repository.ContentReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentReportService {

    private final ContentReportRepository reportRepository;
    private final UserService userService;
    private final RatingService ratingService;
    private final CommentService commentService;
    private final NotificationService notificationService;

    @Transactional
    public ReportResponse createReport(User reporter, ReportRequest request) {
        ContentReport report = new ContentReport();
        report.setReporter(reporter);
        report.setTargetType(request.getTargetType());
        report.setTargetId(request.getTargetId());
        report.setReason(request.getReason());
        return mapToResponse(reportRepository.save(report));
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> getReports(ContentReport.ReportStatus status) {
        List<ContentReport> reports = status != null
                ? reportRepository.findByStatusOrderByCreatedAtDesc(status)
                : reportRepository.findAllByOrderByCreatedAtDesc();
        return reports.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public ReportResponse resolveReport(Long reportId) {
        ContentReport report = findOrThrow(reportId);
        report.setStatus(ContentReport.ReportStatus.RESOLVED);
        reportRepository.save(report);

        // Obtener el autor del contenido antes de borrarlo y notificarle
        try {
            User author = null;
            if (report.getTargetType() == ContentReport.TargetType.RATING) {
                author = ratingService.getRatingAuthor(report.getTargetId());
                ratingService.adminDeleteRating(report.getTargetId());
            } else if (report.getTargetType() == ContentReport.TargetType.COMMENT) {
                author = commentService.getCommentAuthor(report.getTargetId());
                commentService.adminDelete(report.getTargetId());
            }
            if (author != null) {
                notificationService.notifyContentRemoved(author, report);
            }
        } catch (RuntimeException ignored) {
            // El contenido ya podría haber sido eliminado previamente
        }

        return mapToResponse(report);
    }

    @Transactional
    public ReportResponse dismissReport(Long reportId) {
        ContentReport report = findOrThrow(reportId);
        report.setStatus(ContentReport.ReportStatus.DISMISSED);
        return mapToResponse(reportRepository.save(report));
    }

    private ContentReport findOrThrow(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reporte no encontrado"));
    }

    private ReportResponse mapToResponse(ContentReport report) {
        return ReportResponse.builder()
                .id(report.getId())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .reason(report.getReason())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .reporter(userService.mapToUserResponse(report.getReporter()))
                .build();
    }
}
