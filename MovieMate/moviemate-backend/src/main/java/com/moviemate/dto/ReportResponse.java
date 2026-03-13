package com.moviemate.dto;

import com.moviemate.entity.ContentReport;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReportResponse {

    private Long id;
    private ContentReport.TargetType targetType;
    private Long targetId;
    private ContentReport.ReportReason reason;
    private ContentReport.ReportStatus status;
    private LocalDateTime createdAt;
    private UserResponse reporter;
}
