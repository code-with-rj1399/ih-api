package ai.interviewhq.api.web;

import ai.interviewhq.api.domain.CrawlPage;
import ai.interviewhq.api.service.CrawlService;
import ai.interviewhq.api.web.dto.CrawlDtos.UpsertCrawlPageRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pages")
public class CrawlPageController {

    private final CrawlService crawlService;

    public CrawlPageController(CrawlService crawlService) {
        this.crawlService = crawlService;
    }

    @PutMapping
    public CrawlPage upsert(@Valid @RequestBody UpsertCrawlPageRequest request) {
        return crawlService.upsertPage(request);
    }

    @GetMapping("/{urlHash}")
    public CrawlPage get(@PathVariable String urlHash) {
        return crawlService.getPage(urlHash);
    }

    @GetMapping
    public CrawlPage findByUrl(@RequestParam String url) {
        return crawlService.findPageByUrl(url)
                .orElseThrow(() -> new NotFoundException("Crawl page not found for url"));
    }
}
