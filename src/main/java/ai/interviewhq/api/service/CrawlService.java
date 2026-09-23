package ai.interviewhq.api.service;

import ai.interviewhq.api.domain.CrawlPage;
import ai.interviewhq.api.domain.CrawlPageStatus;
import ai.interviewhq.api.domain.CrawlRun;
import ai.interviewhq.api.domain.CrawlRunStatus;
import ai.interviewhq.api.dynamo.CrawlPageRepository;
import ai.interviewhq.api.dynamo.CrawlRunRepository;
import ai.interviewhq.api.dynamo.PageResult;
import ai.interviewhq.api.hashing.Hashes;
import ai.interviewhq.api.web.NotFoundException;
import ai.interviewhq.api.web.dto.CrawlDtos.UpsertCrawlPageRequest;
import ai.interviewhq.api.web.dto.CrawlDtos.UpsertCrawlRunRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class CrawlService {

    private static final DateTimeFormatter RUN_ID = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
            .withZone(ZoneOffset.UTC);

    private final CrawlRunRepository crawlRunRepository;
    private final CrawlPageRepository crawlPageRepository;

    public CrawlService(CrawlRunRepository crawlRunRepository, CrawlPageRepository crawlPageRepository) {
        this.crawlRunRepository = crawlRunRepository;
        this.crawlPageRepository = crawlPageRepository;
    }

    public CrawlRun upsertRun(UpsertCrawlRunRequest request) {
        Instant now = Instant.now();
        String runId = request.runId() == null || request.runId().isBlank()
                ? RUN_ID.format(now)
                : request.runId().trim();
        CrawlRun run = crawlRunRepository.findById(runId).orElseGet(CrawlRun::new);
        run.setRunId(runId);
        if (run.getStartedAt() == null) {
            run.setStartedAt(request.startedAt() == null ? now : request.startedAt());
        } else if (request.startedAt() != null) {
            run.setStartedAt(request.startedAt());
        }
        run.setCompletedAt(request.completedAt());
        run.setStatus(request.status() == null ? CrawlRunStatus.RUNNING : CrawlRunStatus.from(request.status()));
        run.setSourcesAttempted(request.sourcesAttempted());
        run.setPagesDiscovered(request.pagesDiscovered());
        run.setPagesCrawled(request.pagesCrawled());
        run.setQuestionsExtracted(request.questionsExtracted());
        run.setQuestionsAccepted(request.questionsAccepted());
        run.setQuestionsRejected(request.questionsRejected());
        run.setErrors(request.errors());
        crawlRunRepository.save(run);
        return run;
    }

    public CrawlRun getRun(String runId) {
        return crawlRunRepository.findById(runId)
                .orElseThrow(() -> new NotFoundException("Crawl run not found: " + runId));
    }

    public PageResult<CrawlRun> listRuns(int limit, String cursor) {
        return crawlRunRepository.findRecent(limit <= 0 ? 25 : Math.min(limit, 100), cursor);
    }

    public CrawlPage upsertPage(UpsertCrawlPageRequest request) {
        Instant now = Instant.now();
        String urlHash = Hashes.urlHash(request.url());
        CrawlPage page = crawlPageRepository.findByUrlHash(urlHash).orElseGet(CrawlPage::new);
        if (page.getFirstSeenAt() == null) {
            page.setFirstSeenAt(now);
        }
        page.setUrl(request.url().trim());
        page.setUrlHash(urlHash);
        page.setSourceId(request.sourceId());
        page.setHttpStatus(request.httpStatus());
        page.setContentHash(request.contentHash());
        page.setLastCrawledAt(request.lastCrawledAt() == null ? now : request.lastCrawledAt());
        page.setLastModifiedAt(request.lastModifiedAt());
        page.setCrawlStatus(request.crawlStatus() == null
                ? CrawlPageStatus.SUCCESS
                : CrawlPageStatus.from(request.crawlStatus()));
        crawlPageRepository.save(page);
        return page;
    }

    public CrawlPage getPage(String urlHash) {
        return crawlPageRepository.findByUrlHash(urlHash)
                .orElseThrow(() -> new NotFoundException("Crawl page not found: " + urlHash));
    }

    public Optional<CrawlPage> findPageByUrl(String url) {
        return crawlPageRepository.findByUrlHash(Hashes.urlHash(url));
    }
}
