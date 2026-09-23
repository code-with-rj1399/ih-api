package ai.interviewhq.api.service;

import ai.interviewhq.api.domain.SourceSeed;
import ai.interviewhq.api.dynamo.PageResult;
import ai.interviewhq.api.dynamo.SourceSeedRepository;
import ai.interviewhq.api.web.NotFoundException;
import ai.interviewhq.api.web.dto.SourceSeedDtos.PatchSeedRequest;
import ai.interviewhq.api.web.dto.SourceSeedDtos.UpsertSeedRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class SourceSeedService {

    private final SourceSeedRepository repository;

    public SourceSeedService(SourceSeedRepository repository) {
        this.repository = repository;
    }

    public SourceSeed upsert(String sourceId, UpsertSeedRequest request) {
        Instant now = Instant.now();
        SourceSeed existing = repository.find(sourceId, request.seedId()).orElse(null);
        SourceSeed seed = existing == null ? new SourceSeed() : existing;
        seed.setSourceId(sourceId);
        seed.setSeedId(request.seedId());
        seed.setName(request.name());
        seed.setUrl(request.url());
        seed.setEnabled(request.enabled() == null || request.enabled());
        seed.setCrawlStrategy(request.crawlStrategy() == null ? "LISTING" : request.crawlStrategy());
        seed.setCrawlIntervalMinutes(request.crawlIntervalMinutes());
        if (seed.getCreatedAt() == null) {
            seed.setCreatedAt(now);
        }
        seed.setUpdatedAt(now);
        repository.save(seed);
        return seed;
    }

    public SourceSeed patch(String sourceId, String seedId, PatchSeedRequest request) {
        SourceSeed seed = repository.find(sourceId, seedId)
                .orElseThrow(() -> new NotFoundException("Source seed not found: " + sourceId + "/" + seedId));
        if (request.enabled() != null) {
            seed.setEnabled(request.enabled());
        }
        if (request.crawlStrategy() != null) {
            seed.setCrawlStrategy(request.crawlStrategy());
        }
        if (request.crawlIntervalMinutes() != null) {
            seed.setCrawlIntervalMinutes(request.crawlIntervalMinutes());
        }
        if (request.lastCrawledAt() != null) {
            seed.setLastCrawledAt(request.lastCrawledAt());
        }
        seed.setUpdatedAt(Instant.now());
        repository.save(seed);
        return seed;
    }

    public SourceSeed get(String sourceId, String seedId) {
        return repository.find(sourceId, seedId)
                .orElseThrow(() -> new NotFoundException("Source seed not found: " + sourceId + "/" + seedId));
    }

    public List<SourceSeed> listBySource(String sourceId) {
        return repository.findBySource(sourceId);
    }

    public PageResult<SourceSeed> listAll(int limit, String cursor) {
        return repository.findAll(limit <= 0 ? 50 : Math.min(limit, 200), cursor);
    }

    public void delete(String sourceId, String seedId) {
        get(sourceId, seedId);
        repository.delete(sourceId, seedId);
    }
}
