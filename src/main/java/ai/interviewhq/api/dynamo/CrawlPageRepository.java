package ai.interviewhq.api.dynamo;

import ai.interviewhq.api.domain.CrawlPage;
import ai.interviewhq.api.domain.Keys;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CrawlPageRepository {

    private final DynamoOperations dynamo;

    public CrawlPageRepository(DynamoOperations dynamo) {
        this.dynamo = dynamo;
    }

    public Optional<CrawlPage> findByUrlHash(String urlHash) {
        return dynamo.get(Keys.pagePk(urlHash), Keys.ENTITY).map(ItemMapper::toCrawlPage);
    }

    public void save(CrawlPage page) {
        dynamo.put(ItemMapper.fromCrawlPage(page));
    }

    public boolean createIfAbsent(CrawlPage page) {
        return dynamo.putIfAbsent(ItemMapper.fromCrawlPage(page));
    }
}
