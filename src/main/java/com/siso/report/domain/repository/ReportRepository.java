package com.siso.report.domain.repository;

import com.siso.report.domain.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
