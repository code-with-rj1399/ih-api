package ai.interviewhq.api.web;

import ai.interviewhq.api.domain.SourceSeed;
import ai.interviewhq.api.dynamo.PageResult;
import ai.interviewhq.api.service.SourceSeedService;
import ai.interviewhq.api.web.dto.PageResponse;
import ai.interviewhq.api.web.dto.SourceSeedDtos.PatchSeedRequest;
import ai.interviewhq.api.web.dto.SourceSeedDtos.UpsertSeedRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class SourceSeedController {

    private final SourceSeedService seeds;

    public SourceSeedController(SourceSeedService seeds) {
        this.seeds = seeds;
    }

    @GetMapping("/api/v1/sources/seeds")
    public PageResponse<SourceSeed> listAll(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String cursor) {
        PageResult<SourceSeed> page = seeds.listAll(limit, cursor);
        return PageResponse.of(page.getItems(), page.getNextCursor());
    }

    @GetMapping("/api/v1/sources/{sourceId}/seeds")
    public Map<String, List<SourceSeed>> list(@PathVariable String sourceId) {
        return Map.of("items", seeds.listBySource(sourceId));
    }

    @GetMapping("/api/v1/sources/{sourceId}/seeds/{seedId}")
    public SourceSeed get(@PathVariable String sourceId, @PathVariable String seedId) {
        return seeds.get(sourceId, seedId);
    }

    @PutMapping("/api/v1/sources/{sourceId}/seeds/{seedId}")
    public SourceSeed upsert(@PathVariable String sourceId,
                             @PathVariable String seedId,
                             @Valid @RequestBody UpsertSeedRequest request) {
        UpsertSeedRequest body = new UpsertSeedRequest(
                seedId,
                request.name(),
                request.url(),
                request.enabled(),
                request.crawlStrategy(),
                request.crawlIntervalMinutes());
        return seeds.upsert(sourceId, body);
    }

    @PatchMapping("/api/v1/sources/{sourceId}/seeds/{seedId}")
    public SourceSeed patch(@PathVariable String sourceId,
                            @PathVariable String seedId,
                            @RequestBody PatchSeedRequest request) {
        return seeds.patch(sourceId, seedId, request);
    }

    @DeleteMapping("/api/v1/sources/{sourceId}/seeds/{seedId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String sourceId, @PathVariable String seedId) {
        seeds.delete(sourceId, seedId);
    }
}
