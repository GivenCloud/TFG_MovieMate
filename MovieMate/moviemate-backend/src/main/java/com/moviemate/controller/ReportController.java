package com.moviemate.controller;

import com.moviemate.dto.ReportRequest;
import com.moviemate.dto.ReportResponse;
import com.moviemate.entity.User;
import com.moviemate.security.CustomUserDetails;
import com.moviemate.service.ContentReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ContentReportService reportService;

    @PostMapping
    public ResponseEntity<ReportResponse> createReport(
            @Valid @RequestBody ReportRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reportService.createReport(user, request));
    }
}
