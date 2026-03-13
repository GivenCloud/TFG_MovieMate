package com.moviemate.repository;

import com.moviemate.entity.ContentReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentReportRepository extends JpaRepository<ContentReport, Long> {

    List<ContentReport> findByStatusOrderByCreatedAtDesc(ContentReport.ReportStatus status);

    List<ContentReport> findAllByOrderByCreatedAtDesc();
}
