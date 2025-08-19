package com.siso.report.domain;

import com.siso.call.domain.Call;
import com.siso.user.domain.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reposts")
@Getter
@NoArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "call_id", nullable = false)
    private Call call;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repoter", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repoted", nullable = false)
    private User reported;

    @Column(name = "report_title", nullable = false, length = 50)
    private String reportTitle;

    @Column(name = "description", nullable = false, columnDefinition = "Text")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "reported_status", nullable = false, length = 20)
    private ReportStatus reportedStatus;
}
