package ai.interviewhq.api.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CrawlRunStatus {
    RUNNING,
    COMPLETED,
    PARTIAL,
    FAILED;

    @JsonValue
    public String canonical() {
        return name();
    }

    @JsonCreator
    public static CrawlRunStatus from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("status is required");
        }
        try {
            return CrawlRunStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown crawl run status '" + value + "'");
        }
    }
}
