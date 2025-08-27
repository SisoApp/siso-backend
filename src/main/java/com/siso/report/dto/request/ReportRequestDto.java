package com.siso.report.dto.request;

import com.siso.report.domain.model.ReportType;
import lombok.Getter;

@Getter
public class ReportRequestDto {
    private Long reportedId;
    private String reportTitle;
    private String description;
    private ReportType reportType;
}
