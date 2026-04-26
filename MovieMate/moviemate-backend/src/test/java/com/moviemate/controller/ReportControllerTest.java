package com.moviemate.controller;

import com.moviemate.dto.ReportRequest;
import com.moviemate.dto.ReportResponse;
import com.moviemate.dto.UserResponse;
import com.moviemate.entity.ContentReport;
import com.moviemate.entity.User;
import com.moviemate.security.CustomUserDetails;
import com.moviemate.service.ContentReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ReportControllerTest {

    private ContentReportService reportService;
    private ReportController reportController;

    @BeforeEach
    void setUp() {
        reportService = mock(ContentReportService.class);
        reportController = new ReportController(reportService);
    }

    @Test
    void createReport_shouldReturnCreated() {
        User user = new User();
        user.setId(5L);
        user.setUsername("mod");
        CustomUserDetails userDetails = new CustomUserDetails(user);

        ReportRequest request = new ReportRequest();
        request.setTargetType(ContentReport.TargetType.RATING);
        request.setTargetId(12L);
        request.setReason(ContentReport.ReportReason.SPAM);

        ReportResponse response = ReportResponse.builder()
                .id(20L)
                .targetType(ContentReport.TargetType.RATING)
                .targetId(12L)
                .reason(ContentReport.ReportReason.SPAM)
                .status(ContentReport.ReportStatus.PENDING)
                .reporter(UserResponse.builder().id(5L).username("mod").build())
                .build();

        when(reportService.createReport(user, request)).thenReturn(response);

        ResponseEntity<ReportResponse> entity = reportController.createReport(request, userDetails);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(entity.getBody()).isEqualTo(response);
        verify(reportService).createReport(user, request);
    }
}
