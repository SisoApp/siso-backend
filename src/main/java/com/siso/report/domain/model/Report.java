package com.siso.report.domain.model;

import com.siso.common.domain.BaseTime;
import com.siso.user.domain.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repoted", nullable = false)
    private User reported;

    @Column(name = "report_title", nullable = false, length = 50)
    private String reportTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 30)
    private ReportType reportType;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    // 양방향 연관 관계 설정
    public void linkReporter(User user) {
        this.reporter = user;
        user.addReporter(this);
    }

    public void linkReported(User user) {
        this.reported = user;
        user.addReported(this);
    }

    @Builder
    public Report(User reporter, User reported, String reportTitle, ReportType reportType, String description) {
        this.reporter = reporter;
        this.reported = reported;
        this.reportTitle = reportTitle;
        this.reportType = reportType;
        this.description = description;
    }
}
