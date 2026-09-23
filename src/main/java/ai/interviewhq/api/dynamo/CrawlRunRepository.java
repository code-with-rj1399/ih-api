package ai.interviewhq.api.dynamo;

import ai.interviewhq.api.domain.CrawlRun;
import ai.interviewhq.api.domain.Keys;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CrawlRunRepository {

    private final DynamoOperations dynamo;

    public CrawlRunRepository(DynamoOperations dynamo) {
        this.dynamo = dynamo;
    }

    public Optional<CrawlRun> findById(String runId) {
        return dynamo.get(Keys.crawlRunPk(runId), Keys.ENTITY).map(ItemMapper::toCrawlRun);
    }

    public void save(CrawlRun run) {
        dynamo.put(ItemMapper.fromCrawlRun(run));
    }

    public boolean createIfAbsent(CrawlRun run) {
        return dynamo.putIfAbsent(ItemMapper.fromCrawlRun(run));
    }

    public PageResult<CrawlRun> findRecent(int limit, String cursor) {
        return dynamo.queryGsi(Keys.GSI4, Keys.GSI4PK, Keys.gsi4pkEntityType(Keys.ENTITY_CRAWL_RUN),
                limit, cursor, false, ItemMapper::toCrawlRun);
    }
}
