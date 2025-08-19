package com.siso.report.dto;

import com.siso.report.domain.Report;
import com.siso.report.domain.ReportStatus;

import java.time.LocalDateTime;

public record ReportResponseDto(
        Long id,
        Long callId,
        Long reporterId,
        Long reportedId,
        String reportTitle,
        String description,
        ReportStatus reportedStatus,
        LocalDateTime createdAt
) {
    public static ReportResponseDto from(Report report) {
        return new ReportResponseDto(
                report.getId(),
                report.getCall().getId(),
                report.getReporter().getId(),
                report.getReported().getId(),
                report.getReportTitle(),
                report.getDescription(),
                report.getReportedStatus(),
                report.getCreatedAt()
        );
    }
}