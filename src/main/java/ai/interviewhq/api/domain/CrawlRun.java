package ai.interviewhq.api.domain;

import java.time.Instant;

public class CrawlRun {

    private String runId;
    private Instant startedAt;
    private Instant completedAt;
    private CrawlRunStatus status;
    private Integer sourcesAttempted;
    private Integer pagesDiscovered;
    private Integer pagesCrawled;
    private Integer questionsExtracted;
    private Integer questionsAccepted;
    private Integer questionsRejected;
    private Integer errors;

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public CrawlRunStatus getStatus() {
        return status;
    }

    public void setStatus(CrawlRunStatus status) {
        this.status = status;
    }

    public Integer getSourcesAttempted() {
        return sourcesAttempted;
    }

    public void setSourcesAttempted(Integer sourcesAttempted) {
        this.sourcesAttempted = sourcesAttempted;
    }

    public Integer getPagesDiscovered() {
        return pagesDiscovered;
    }

    public void setPagesDiscovered(Integer pagesDiscovered) {
        this.pagesDiscovered = pagesDiscovered;
    }

    public Integer getPagesCrawled() {
        return pagesCrawled;
    }

    public void setPagesCrawled(Integer pagesCrawled) {
        this.pagesCrawled = pagesCrawled;
    }

    public Integer getQuestionsExtracted() {
        return questionsExtracted;
    }

    public void setQuestionsExtracted(Integer questionsExtracted) {
        this.questionsExtracted = questionsExtracted;
    }

    public Integer getQuestionsAccepted() {
        return questionsAccepted;
    }

    public void setQuestionsAccepted(Integer questionsAccepted) {
        this.questionsAccepted = questionsAccepted;
    }

    public Integer getQuestionsRejected() {
        return questionsRejected;
    }

    public void setQuestionsRejected(Integer questionsRejected) {
        this.questionsRejected = questionsRejected;
    }

    public Integer getErrors() {
        return errors;
    }

    public void setErrors(Integer errors) {
        this.errors = errors;
    }
}
