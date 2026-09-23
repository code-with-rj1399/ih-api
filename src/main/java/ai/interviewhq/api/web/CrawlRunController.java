package ai.interviewhq.api.web;

import ai.interviewhq.api.domain.CrawlRun;
import ai.interviewhq.api.dynamo.PageResult;
import ai.interviewhq.api.service.CrawlService;
import ai.interviewhq.api.web.dto.CrawlDtos.UpsertCrawlRunRequest;
import ai.interviewhq.api.web.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/crawl-runs")
public class CrawlRunController {

    private final CrawlService crawlService;

    public CrawlRunController(CrawlService crawlService) {
        this.crawlService = crawlService;
    }

    @GetMapping
    public PageResponse<CrawlRun> list(
            @RequestParam(defaultValue = "25") int limit,
            @RequestParam(required = false) String cursor) {
        PageResult<CrawlRun> page = crawlService.listRuns(limit, cursor);
        return PageResponse.of(page.getItems(), page.getNextCursor());
    }

    @GetMapping("/{runId}")
    public CrawlRun get(@PathVariable String runId) {
        return crawlService.getRun(runId);
    }

    @PutMapping
    public CrawlRun upsert(@Valid @RequestBody UpsertCrawlRunRequest request) {
        return crawlService.upsertRun(request);
    }

    @PutMapping("/{runId}")
    public CrawlRun upsertById(@PathVariable String runId, @RequestBody UpsertCrawlRunRequest request) {
        UpsertCrawlRunRequest body = new UpsertCrawlRunRequest(
                runId,
                request.status(),
                request.startedAt(),
                request.completedAt(),
                request.sourcesAttempted(),
                request.pagesDiscovered(),
                request.pagesCrawled(),
                request.questionsExtracted(),
                request.questionsAccepted(),
                request.questionsRejected(),
                request.errors());
        return crawlService.upsertRun(body);
    }
}
