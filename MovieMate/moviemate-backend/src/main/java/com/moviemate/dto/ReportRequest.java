package com.moviemate.dto;

import com.moviemate.entity.ContentReport;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportRequest {

    @NotNull
    private ContentReport.TargetType targetType;

    @NotNull
    private Long targetId;

    @NotNull
    private ContentReport.ReportReason reason;
}
