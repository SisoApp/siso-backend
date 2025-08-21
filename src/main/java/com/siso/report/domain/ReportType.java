package com.siso.report.domain;

public enum ReportType {
    SPAM("스팸성/글/메시지"),
    INAPPROPRIATE("부적절한 내용(욕설,선정적,폭력적"),
    HARASSMENT("괴롭힘,혐오 발언"),
    IMPERSONATION("사칭,도용"),
    ILLEGAL_CONTENT("불법 컨텐츠,마약,불법광고"),
    SEXUAL_CONTENT("성적,음란한 콘텐츠"),
    VIOLENCE("폭력적/위험적 콘텐츠"),
    PRIVACY("개인정보 유출"),
    OTHER("기타(직접 입력)");

    private final String description;

    ReportType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
    }
