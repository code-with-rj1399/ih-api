package ai.interviewhq.api.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CrawlPageStatus {
    SUCCESS,
    SKIPPED,
    FAILED,
    PENDING;

    @JsonValue
    public String canonical() {
        return name();
    }

    @JsonCreator
    public static CrawlPageStatus from(String value) {
        if (value == null || value.isBlank()) {
            return PENDING;
        }
        try {
            return CrawlPageStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown crawl page status '" + value + "'");
        }
    }
}
