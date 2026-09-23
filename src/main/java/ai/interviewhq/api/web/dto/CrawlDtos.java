package ai.interviewhq.api.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public final class CrawlDtos {

    private CrawlDtos() {
    }

    public record UpsertCrawlRunRequest(
            String runId,
            String status,
            Instant startedAt,
            Instant completedAt,
            Integer sourcesAttempted,
            Integer pagesDiscovered,
            Integer pagesCrawled,
            Integer questionsExtracted,
            Integer questionsAccepted,
            Integer questionsRejected,
            Integer errors
    ) {
    }

    public record UpsertCrawlPageRequest(
            @NotBlank String url,
            String sourceId,
            Integer httpStatus,
            String contentHash,
            Instant lastCrawledAt,
            Instant lastModifiedAt,
            String crawlStatus
    ) {
    }
}
