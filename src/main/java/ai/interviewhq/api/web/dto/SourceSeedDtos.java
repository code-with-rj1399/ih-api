package ai.interviewhq.api.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public final class SourceSeedDtos {

    private SourceSeedDtos() {
    }

    public record UpsertSeedRequest(
            String seedId,
            @NotBlank String name,
            @NotBlank String url,
            Boolean enabled,
            String crawlStrategy,
            Integer crawlIntervalMinutes
    ) {
    }

    public record PatchSeedRequest(
            Boolean enabled,
            String crawlStrategy,
            Integer crawlIntervalMinutes,
            Instant lastCrawledAt
    ) {
    }
}
