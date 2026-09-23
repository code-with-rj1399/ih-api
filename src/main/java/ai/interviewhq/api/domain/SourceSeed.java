package ai.interviewhq.api.domain;

import java.time.Instant;

public class SourceSeed {

    private String sourceId;
    private String seedId;
    private String name;
    private String url;
    private boolean enabled = true;
    private String crawlStrategy;
    private Integer crawlIntervalMinutes;
    private Instant lastCrawledAt;
    private Instant createdAt;
    private Instant updatedAt;

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getSeedId() {
        return seedId;
    }

    public void setSeedId(String seedId) {
        this.seedId = seedId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCrawlStrategy() {
        return crawlStrategy;
    }

    public void setCrawlStrategy(String crawlStrategy) {
        this.crawlStrategy = crawlStrategy;
    }

    public Integer getCrawlIntervalMinutes() {
        return crawlIntervalMinutes;
    }

    public void setCrawlIntervalMinutes(Integer crawlIntervalMinutes) {
        this.crawlIntervalMinutes = crawlIntervalMinutes;
    }

    public Instant getLastCrawledAt() {
        return lastCrawledAt;
    }

    public void setLastCrawledAt(Instant lastCrawledAt) {
        this.lastCrawledAt = lastCrawledAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
