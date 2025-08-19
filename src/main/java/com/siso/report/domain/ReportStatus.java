package com.siso.report.domain;

public enum ReportStatus {
    PENDING("진행중"),
    REVIEWING("검토중"),
    ACTIONED("처리 완료"),
    REJECTED("거절됨");

        private final String description;

        ReportStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
}
